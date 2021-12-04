package com.nimbusframework.nimbusaws.wrappers.basic

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class BasicServerlessFunctionFileBuilder(
    private val cron: Boolean,
    processingEnv: ProcessingEnvironment,
    methodInformation: MethodInformation,
    compilingElement: Element,
    processingData: ProcessingData
) : ServerlessFunctionFileBuilder(
    processingEnv,
    methodInformation,
    BasicServerlessFunction::class.java.simpleName,
    BasicEvent::class.java,
    compilingElement,
    null,
    null,
    processingData
) {
    override fun getGeneratedClassName(): String {
        return "BasicServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {}

    override fun writeFunction(inputParam: Param, eventParam: Param) {

        if (eventParam.exists()) {
            write("$eventSimpleName event = new $eventSimpleName(requestId);")
        }

        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        val methodName = methodInformation.methodName
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
        } else if (methodInformation.parameters.size > 2) {
            compilationError("Too many parameters for BasicServerlessFunction, maximum 2 of type T, and BasicEvent")
        }
    }
}
