package wrappers

import annotation.models.processing.MethodInformation
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

abstract class ServerlessFunctionFileBuilder(
        protected val processingEnv: ProcessingEnvironment,
        protected val methodInformation: MethodInformation,
        private val functionType: String,
        eventType: ServerlessEvent,
        private val compilingElement: Element
) {

    private var tabLevel: Int = 0

    private val eventCanonicalName = eventType::class.java.canonicalName
    private val eventSimpleName = eventType::class.java.simpleName

    private var out: PrintWriter? = null

    protected val messager: Messager = processingEnv.messager

    protected abstract fun getGeneratedClassName(): String

    protected abstract fun writeImports()

    protected abstract fun writeInputs(param: Param)

    protected abstract fun writeFunction(inputParam: Param, eventParam: Param)

    protected abstract fun writeOutput()

    protected abstract fun writeHandleError()

    open fun eventCannotBeList(): Boolean {
        return true
    }

    fun createClass() {
        if (!customFunction()) {
            try {

                val params = findParamIndexes()

                isValidFunction(params)

                val builderFile = processingEnv.filer.createSourceFile(getGeneratedClassName())
                out = PrintWriter(builderFile.openWriter())

                val packageName = findPackageName(methodInformation.qualifiedName)

                if (packageName != "") write("package $packageName;")

                writeImports()

                write("public class ${getGeneratedClassName()} {")

                write()

                write("public void nimbusHandle(InputStream input, OutputStream output, Context context) {")

                write("ObjectMapper objectMapper = new ObjectMapper();")
                write("try {")

                write("String jsonString = new BufferedReader(new InputStreamReader(input)).lines().collect(Collectors.joining(\"\\n\"));")

                writeInputs(params.inputParam)

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

                writeFunction(params.inputParam, params.eventParam)

                writeOutput()

                write("} catch (Exception e) {")

                writeHandleError()

                write("}")
                write("return;")


                write("}")

                write("}")

                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected open fun isValidFunction(functionParams: FunctionParams) {
        val errorPrefix = "Incorrect $functionType parameters."
        if (methodInformation.parameters.size > 2) {
            compilationError("$errorPrefix Too many arguments, can have at most two: T input, $eventSimpleName event.")
        } else if (methodInformation.parameters.size == 2) {
            if (functionParams.eventParam.isEmpty()) {
                compilationError("$errorPrefix Can't have two data input types. Function can have at most two parameters: T input, $eventSimpleName event.")
            } else if (functionParams.inputParam.isEmpty()) {
                compilationError("$errorPrefix Can't have two event input types. Function can have at most two parameters: T input, $eventSimpleName event.")
            }
            if (eventCannotBeList() && functionParams.eventParam.type != null && isAListType(functionParams.eventParam.type!!)) {
                compilationError("$errorPrefix Cannot have a list of $eventSimpleName for a $functionType")
            }
        }
    }

    fun getHandler(): String {
        return if (customFunction()) {
            if (methodInformation.qualifiedName == "") {
                "${methodInformation.className}::${methodInformation.methodName}"
            } else {
                "${methodInformation.qualifiedName}.${methodInformation.className}::${methodInformation.methodName}"
            }
        } else {
            "${getGeneratedClassName()}::nimbusHandle"
        }
    }

    protected fun write(toWrite: String = "") {
        if (toWrite.startsWith("}")) tabLevel--

        var tabs = ""
        for (i in 1..tabLevel) {
            tabs += "\t"
        }
        out?.println("$tabs$toWrite")

        if (toWrite.endsWith("{")) tabLevel++
    }

    private fun customFunction(): Boolean {
        val params = methodInformation.parameters
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

    private fun findParamIndexes(): FunctionParams {
        val functionParams = FunctionParams()
        for ((paramIndex, param) in methodInformation.parameters.withIndex()) {
            if (param.toString() == eventCanonicalName) {
                functionParams.eventParam = Param(param, paramIndex)
            } else if (isAListType(param) && param.toString().contains(eventCanonicalName)) {
                functionParams.eventParam = Param(param, paramIndex)
            } else {
                functionParams.inputParam = Param(param, paramIndex)
            }
        }
        return functionParams
    }


    protected fun findPackageName(qualifiedName: String): String {
        val lastDot = qualifiedName.lastIndexOf('.')
        return if (lastDot > 0) {
            qualifiedName.substring(0, lastDot)
        } else {
            ""
        }
    }

    protected fun isAListType(type: TypeMirror): Boolean {
        return type.toString().startsWith("java.util.List<")
    }

    protected fun findListType(list: TypeMirror): String {
        return list.toString().substringAfter("<").substringBefore(">")
    }

    protected data class Param(val type: TypeMirror?, val index: Int) {
        fun isEmpty(): Boolean {return type == null}
    }

    protected data class FunctionParams(
            var inputParam: Param = Param(null, 0),
            var eventParam: Param = Param(null, 0)
    )

    protected fun compilationError(msg: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, compilingElement)
    }

}