package com.nimbusframework.nimbusaws.wrappers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.clients.AwsClientBinder
import com.nimbusframework.nimbusaws.clients.AwsInternalClientBuilder
import com.nimbusframework.nimbusaws.cloudformation.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.lambda.handlers.HandlerInformationProvider
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.eventabstractions.ServerlessEvent
import java.io.File
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

abstract class ServerlessFunctionFileBuilder(
    protected val processingEnv: ProcessingEnvironment,
    protected val fileBuilderMethodInformation: FileBuilderMethodInformation,
    private val functionType: String,
    eventType: Class<out ServerlessEvent>?,
    private val compilingElement: Element,
    inputType: Class<*>?,
    returnType: Class<*>?,
    protected val classForReflectionService: ClassForReflectionService
): FileBuilder(), HandlerInformationProvider {

    private val messager: Messager = processingEnv.messager

    private val primitiveToBoxedMap = mapOf(
            Pair("byte", "java.lang.Byte"),
            Pair("short", "java.lang.Short"),
            Pair("int", "java.lang.Integer"),
            Pair("long", "java.lang.Long"),
            Pair("float", "java.lang.Float"),
            Pair("double", "java.lang.Double"),
            Pair("char", "java.lang.Char"),
            Pair("boolean", "java.lang.Boolean"),
            Pair("void", "java.lang.Void")
    )

    protected val eventCanonicalName: String = if (eventType != null) eventType.canonicalName else ""
    protected val eventSimpleName: String = if (eventType != null) eventType.simpleName else ""

    protected val params = findParamIndexes(fileBuilderMethodInformation, eventCanonicalName)

    protected val voidMethodReturn = fileBuilderMethodInformation.returnType.kind == TypeKind.NULL || fileBuilderMethodInformation.returnType.kind == TypeKind.VOID

    protected val returnTypeCanonicalName: String
    protected val returnTypeSimpleName: String
    protected val inputTypeSimpleName: String
    protected val inputTypeCanonicalName: String

    init {
        if (inputType == null) {
            val inputTypeString = params.inputParam.canonicalName()
            if (primitiveToBoxedMap.containsKey(inputTypeString)) {
                inputTypeCanonicalName = primitiveToBoxedMap[inputTypeString]!!
                inputTypeSimpleName = inputTypeCanonicalName.substringAfterLast('.')
            } else {
                inputTypeCanonicalName = params.inputParam.canonicalName()
                inputTypeSimpleName = params.inputParam.simpleName()
            }
        } else {
            inputTypeCanonicalName = inputType.canonicalName
            inputTypeSimpleName = inputType.simpleName

            // If the input type isn't null we use reflection to deserialise the class from AWS (if using a custom runtime)
            classForReflectionService.addClassForReflection(inputType)
        }
        if (returnType == null) {
            if (voidMethodReturn) {
                returnTypeCanonicalName = "java.lang.Void"
                returnTypeSimpleName = "Void"
            } else {
                val returnTypeString = fileBuilderMethodInformation.returnType.toString()
                if (primitiveToBoxedMap.containsKey(returnTypeString)) {
                    returnTypeCanonicalName = primitiveToBoxedMap[returnTypeString]!!
                    returnTypeSimpleName = returnTypeCanonicalName.substringAfterLast('.')
                } else {
                    returnTypeCanonicalName = returnTypeString
                    returnTypeSimpleName = returnTypeString.substringAfterLast('.')
                }
            }
        } else {
            returnTypeCanonicalName = returnType.canonicalName
            returnTypeSimpleName = returnType.simpleName

            // If the return type isn't null we use reflection to serialise the class (if using a custom runtime)
            classForReflectionService.addClassForReflection(returnType)
        }

        addPotentialReflectionTargets()
    }

    private fun addPotentialReflectionTargets() {
        if (params.inputParam.exists()) {
            classForReflectionService.addClassForReflection(params.inputParam.type)
        }
        val returnTypeString = fileBuilderMethodInformation.returnType.toString()
        if (!primitiveToBoxedMap.containsKey(returnTypeString)) {
            classForReflectionService.addClassForReflection(fileBuilderMethodInformation.returnType)
        }
    }

    protected val voidReturnType = returnTypeSimpleName == "Void"

    protected abstract fun generateClassName(): String

    val generatedClassName by lazy { generateClassName() }

    protected abstract fun writeImports()

    protected abstract fun writeFunction(inputParam: Param, eventParam: Param)

    protected abstract fun writeHandleError()

    protected open fun writeCustomExceptionHandler() {}


    open fun eventCannotBeList(): Boolean {
        return true
    }

    fun getGeneratedClassInformation(): AwsMethodInformation {
        return AwsMethodInformation(
            fileBuilderMethodInformation.packageName,
            generatedClassName,
            inputTypeCanonicalName,
            returnTypeCanonicalName
        )
    }

    override fun getClassFilePath(): String {
        val className = generatedClassName
        val packageName = fileBuilderMethodInformation.packageName
        val packagePath = packageName.replace(".", File.separator)

        return if (packageName.isEmpty()) {
            className
        } else {
            "$packagePath${File.separator}$className"
        }
    }

    open fun createClass() {
        if (!customFunction()) {
            try {


                isValidFunction(params)

                val className = generatedClassName
                val builderFile = processingEnv.filer.createSourceFile(className)

                out = PrintWriter(builderFile.openWriter())


                if (fileBuilderMethodInformation.packageName != "") write("package ${fileBuilderMethodInformation.packageName};")

                writeImports()

                write("import ${RequestHandler::class.qualifiedName};")
                write("import ${Context::class.qualifiedName};")
                write("import ${AwsInternalClientBuilder::class.qualifiedName};")
                write("import ${ClientBinder::class.qualifiedName};")
                write("import ${AwsClientBinder::class.qualifiedName};")
                write("import $inputTypeCanonicalName;")
                write("import $returnTypeCanonicalName; ")

                if (params.inputParam.type != null && isAListType(params.inputParam.type!!)) {
                    write("import ${findListType(params.inputParam.type!!)};")
                    write("import java.util.List;")
                } else if (!primitiveToBoxedMap.containsKey(params.inputParam.canonicalName())) {
                    write("import ${params.inputParam.canonicalName()};")
                }

                if (eventCanonicalName != "") {
                    write("import $eventCanonicalName;")
                }

                if (fileBuilderMethodInformation.packageName.isNotBlank()) {
                    write("import ${fileBuilderMethodInformation.packageName}.${fileBuilderMethodInformation.className};")
                }

                write("public class $generatedClassName implements RequestHandler<$inputTypeSimpleName, $returnTypeSimpleName>{")

                write()

                write("private final ${fileBuilderMethodInformation.className} handler;")


                write("public ${generatedClassName}() {")
                write("ClientBinder.INSTANCE.setInternalBuilder(AwsInternalClientBuilder.INSTANCE);")
                write("AwsClientBinder.INSTANCE.setInternalBuilder(AwsInternalClientBuilder.INSTANCE);")
                if (fileBuilderMethodInformation.customFactoryQualifiedName == null) {
                    write("handler = new ${fileBuilderMethodInformation.className}();")
                } else {
                    write("handler = new ${fileBuilderMethodInformation.customFactoryQualifiedName}().create();")
                }
                write("}")

                write("public $returnTypeSimpleName handleRequest($inputTypeSimpleName input, Context context) {")

                write("try {")

                write("String requestId = context.getAwsRequestId();")

                writeFunction(params.inputParam, params.eventParam)

                writeCustomExceptionHandler()

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
        if (fileBuilderMethodInformation.parameters.size > 2) {
            compilationError("$errorPrefix Too many arguments, can have at most two: T input, $eventSimpleName event.")
        } else if (fileBuilderMethodInformation.parameters.size == 2) {
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

    override fun getHandler(): String {
        return if (customFunction()) {
            if (fileBuilderMethodInformation.packageName == "") {
                "${fileBuilderMethodInformation.className}::${fileBuilderMethodInformation.methodName}"
            } else {
                "${fileBuilderMethodInformation.packageName}.${fileBuilderMethodInformation.className}::${fileBuilderMethodInformation.methodName}"
            }
        } else {
            if (fileBuilderMethodInformation.packageName == "") {
                "${generatedClassName}::handleRequest"
            } else {
                fileBuilderMethodInformation.packageName + ".${generatedClassName}::handleRequest"
            }
        }
    }

    protected fun customFunction(): Boolean {
        val params = fileBuilderMethodInformation.parameters
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

    protected fun compilationError(msg: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, compilingElement)
    }

}
