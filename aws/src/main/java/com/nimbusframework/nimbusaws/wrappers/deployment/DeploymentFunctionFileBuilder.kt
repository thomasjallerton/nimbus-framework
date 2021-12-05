package com.nimbusframework.nimbusaws.wrappers.deployment

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class DeploymentFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    methodInformation: MethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    methodInformation,
    AfterDeployment::class.java.simpleName,
    BasicEvent::class.java,
    compilingElement,
    Void::class.java,
    null,
    classForReflectionService
) {
    override fun generateClassName(): String {
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
            write("return null;")
        } else {
            write("return result;")
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
