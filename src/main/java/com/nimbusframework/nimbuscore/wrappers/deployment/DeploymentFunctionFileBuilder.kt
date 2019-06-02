package com.nimbusframework.nimbuscore.wrappers.deployment

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.basic.models.BasicEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class DeploymentFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        AfterDeployment::class.java.simpleName,
        BasicEvent::class.java,
        compilingElement,
        Void::class.java,
        null,
        nimbusState
) {
    override fun getGeneratedClassName(): String {
        return "AfterDeployment${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {}

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        if (eventParam.exists()) {
            write("$eventSimpleName event = new $eventSimpleName(requestId);")
        }

        val methodName = methodInformation.methodName
        when {
            eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName();")
            else -> write("${callPrefix}handler.$methodName(event);")
        }

        if (voidMethodReturn) {
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
        if (!functionParams.inputParam.doesNotExist()) {
            compilationError("Cannot have a custom user type in an AfterDeployment function")
        }
    }
}