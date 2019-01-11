package wrappers

import annotation.models.processing.MethodInformation
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror

abstract class ServerlessFunctionFileBuilder(
        protected val processingEnv: ProcessingEnvironment,
        protected val methodInformation: MethodInformation
) {

    private var tabLevel: Int = 0

    protected var out: PrintWriter? = null

    protected val messager: Messager = processingEnv.messager

    protected abstract fun getGeneratedClassName(): String

    protected abstract fun isValidFunction()

    protected abstract fun writeImports()

    protected abstract fun writeInputs(inputParam: InputParam)

    protected abstract fun writeFunction(inputParam: InputParam)

    protected abstract fun writeOutput()

    protected abstract fun writeHandleError()

    fun createClass() {
        if (!customFunction()) {
            try {

                isValidFunction()

                val inputParam = findInputTypeAndIndex()

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

                writeInputs(inputParam)

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

                writeFunction(inputParam)

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

    protected fun customFunction(): Boolean {
        val params = methodInformation.parameters
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

    protected fun findInputTypeAndIndex(): InputParam {
        var inputParamIndex = 0
        for (param in methodInformation.parameters) {
            if (param.toString() != "wrappers.http.models.HttpEvent") {
                return InputParam(param, inputParamIndex)
            } else {
                inputParamIndex++
            }
        }
        return InputParam(null, 0)
    }

    protected data class InputParam(val type: TypeMirror?, val index: Int)

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
}