package com.nimbusframework.nimbusaws.wrappers.basic

import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class BasicServerlessFunctionFileBuilder(
    private val cron: Boolean,
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    BasicServerlessFunction::class.java.simpleName,
    BasicEvent::class.java,
    compilingElement,
    null,
    null,
    classForReflectionService
) {
    override fun generateClassName(): String {
        return "BasicServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {}

    override fun writeFunction(inputParam: Param, eventParam: Param) {

        if (eventParam.exists()) {
            write("$eventSimpleName event = new $eventSimpleName(requestId);")
        }

        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${fileBuilderMethodInformation.returnType} result = "
        }

        val methodName = fileBuilderMethodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName();")
            inputParam.doesNotExist() -> write("${callPrefix}handler.$methodName(event);")
            eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName(input);")
            inputParam.index == 0 -> write("${callPrefix}handler.$methodName(input, event);")
            else -> write("${callPrefix}handler.$methodName(event, input);")
        }


        if (!voidReturnType) {
            write("return result;")
        } else {
            write("return null;")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }

    override fun isValidFunction(functionParams: FunctionParams) {
        if (cron && !functionParams.inputParam.doesNotExist()) {
            compilationError("Cannot have a custom user type parameter in a BasicServerlessFunction")
        } else if (fileBuilderMethodInformation.parameters.size > 2) {
            compilationError("Too many parameters for BasicServerlessFunction, maximum 2 of type T, and BasicEvent")
        }
    }
}
