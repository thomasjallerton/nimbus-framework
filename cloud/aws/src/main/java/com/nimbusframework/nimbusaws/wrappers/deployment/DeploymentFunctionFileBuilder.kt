package com.nimbusframework.nimbusaws.wrappers.deployment

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class DeploymentFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    AfterDeployment::class.java.simpleName,
    BasicEvent::class.java,
    compilingElement,
    Void::class.java,
    null,
    classForReflectionService
) {
    override fun generateClassName(): String {
        return "AfterDeployment${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {}

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${fileBuilderMethodInformation.returnType} result = "
        }

        if (eventParam.exists()) {
            write("$eventSimpleName event = new $eventSimpleName(requestId);")
        }

        val methodName = fileBuilderMethodInformation.methodName
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

    override fun isValidFunction(functionParams: FunctionParams) {
        if (!functionParams.inputParam.doesNotExist()) {
            compilationError("Cannot have a custom user type in an AfterDeployment function")
        }
    }
}
