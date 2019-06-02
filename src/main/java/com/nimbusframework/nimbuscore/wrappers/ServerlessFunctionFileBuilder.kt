package com.nimbusframework.nimbuscore.wrappers

import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import java.io.File
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

abstract class ServerlessFunctionFileBuilder(
        protected val processingEnv: ProcessingEnvironment,
        protected val methodInformation: MethodInformation,
        private val functionType: String,
        eventType: Class<out ServerlessEvent>?,
        private val compilingElement: Element,
        inputType: Class<out Any>?,
        returnType: Class<out Any>?,
        private val nimbusState: NimbusState
) {

    private var tabLevel: Int = 0

    protected val eventCanonicalName: String = if (eventType != null) eventType.canonicalName else ""
    protected val eventSimpleName: String = if (eventType != null) eventType.simpleName else ""

    protected var out: PrintWriter? = null

    protected val params = findParamIndexes()

    private val messager: Messager = processingEnv.messager

    protected val voidMethodReturn = methodInformation.returnType.kind == TypeKind.NULL || methodInformation.returnType.kind == TypeKind.VOID

    protected val returnTypeCanonicalName: String
    protected val returnTypeSimpleName: String
    protected val inputTypeSimpleName: String
    protected val inputTypeCanonicalName: String

    init {
        if (inputType == null) {
            inputTypeCanonicalName = params.inputParam.canonicalName()
            inputTypeSimpleName = params.inputParam.simpleName()
        } else {
            inputTypeCanonicalName = inputType.canonicalName
            inputTypeSimpleName = inputType.simpleName
        }
        if (returnType == null) {
            if (voidMethodReturn) {
                returnTypeCanonicalName = "java.lang.Void"
                returnTypeSimpleName = "Void"
            } else {
                returnTypeCanonicalName = methodInformation.returnType.toString()
                returnTypeSimpleName = methodInformation.returnType.toString().substringAfterLast('.')
            }
        } else {
            returnTypeCanonicalName = returnType.canonicalName
            returnTypeSimpleName = returnType.simpleName
        }
    }

    protected val voidReturnType = returnTypeSimpleName == "Void"

    protected abstract fun getGeneratedClassName(): String

    protected abstract fun writeImports()

    protected abstract fun writeFunction(inputParam: Param, eventParam: Param)

    protected abstract fun writeHandleError()

    open fun eventCannotBeList(): Boolean {
        return true
    }

    fun classFilePath(): String {
        val className = getGeneratedClassName()
        val packageName = findPackageName(methodInformation.packageName)
        val packagePath = packageName.replace(".", File.separator)

        return if (packageName.isEmpty()) {
            className
        } else {
            "$packagePath${File.separator}$className"
        }
    }

    fun handlerFile(): String {
        return getGeneratedClassName() + ".jar"
    }

    open fun createClass() {
        if (!customFunction()) {
            try {


                isValidFunction(params)

                val className = getGeneratedClassName()
                val builderFile = processingEnv.filer.createSourceFile(className)

                out = PrintWriter(builderFile.openWriter())

                val packageName = findPackageName(methodInformation.packageName)

                if (packageName != "") write("package $packageName;")

                writeImports()

                write("import com.amazonaws.services.lambda.runtime.RequestHandler;")
                write("import com.amazonaws.services.lambda.runtime.Context;")
                write("import $inputTypeCanonicalName;")
                write("import $returnTypeCanonicalName; //return type")
                if (params.inputParam.type != null && isAListType(params.inputParam.type!!)) {
                    write("import ${findListType(params.inputParam.type!!)};")
                } else {
                    write("import ${params.inputParam.canonicalName()};")
                }

                if (eventCanonicalName != "") {
                    write("import $eventCanonicalName;")
                }
                if (methodInformation.packageName.isNotBlank()) {
                    write("import ${methodInformation.packageName}.${methodInformation.className};")
                }

                write("public class ${getGeneratedClassName()} implements RequestHandler<$inputTypeSimpleName, $returnTypeSimpleName>{")

                write()

                write("public $returnTypeSimpleName handleRequest($inputTypeSimpleName input, Context context) {")

                write("try {")

                write("String requestId = context.getAwsRequestId();")

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

                writeFunction(params.inputParam, params.eventParam)

                write("} catch (Exception e) {")

                writeHandleError()

                write("}")

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
            if (functionParams.eventParam.doesNotExist()) {
                compilationError("$errorPrefix Can't have two data input types. Function can have at most two parameters: T input, $eventSimpleName event.")
            } else if (functionParams.inputParam.doesNotExist()) {
                compilationError("$errorPrefix Can't have two event input types. Function can have at most two parameters: T input, $eventSimpleName event.")
            }
            if (eventCannotBeList() && functionParams.eventParam.type != null && isAListType(functionParams.eventParam.type!!)) {
                compilationError("$errorPrefix Cannot have a list of $eventSimpleName for a $functionType")
            }
            if (!eventCannotBeList() &&
                    functionParams.eventParam.type != null &&
                    functionParams.inputParam.type != null &&
                    isAListType(functionParams.eventParam.type!!) &&
                    !isAListType(functionParams.inputParam.type!!)) {
                compilationError("Cannot have an event list argument without the custom user type also being a list for a $functionType.")
            }
        }
    }

    fun getHandler(): String {
        return if (customFunction()) {
            if (methodInformation.packageName == "") {
                "${methodInformation.className}::${methodInformation.methodName}"
            } else {
                "${methodInformation.packageName}.${methodInformation.className}::${methodInformation.methodName}"
            }
        } else {
            if (methodInformation.packageName.contains(".")) {
                methodInformation.packageName.substringBeforeLast(".") + ".${getGeneratedClassName()}::handleRequest"
            } else {
                "${getGeneratedClassName()}::handleRequest"
            }
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
        fun doesNotExist(): Boolean {
            return type == null
        }

        fun exists(): Boolean {
            return type != null
        }

        fun canonicalName(): String {
            if (type?.kind == TypeKind.VOID) return "java.lang.Void"
            return type?.toString() ?: "java.lang.Void"
        }

        fun simpleName(): String {
            if (type?.kind == TypeKind.VOID) return "Void"
            return type?.toString()?.substringAfterLast('.') ?: "Void"
        }
    }

    protected data class FunctionParams(
            var inputParam: Param = Param(null, -1),
            var eventParam: Param = Param(null, -1)
    )

    protected fun compilationError(msg: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, compilingElement)
    }

}