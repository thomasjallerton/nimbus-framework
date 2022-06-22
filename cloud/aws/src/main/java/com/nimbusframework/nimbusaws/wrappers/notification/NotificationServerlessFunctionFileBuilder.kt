package com.nimbusframework.nimbusaws.wrappers.notification

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.NotificationEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class NotificationServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    NotificationServerlessFunction::class.java.simpleName,
    NotificationEvent::class.java,
    compilingElement,
    SNSEvent::class.java,
    Void::class.java,
    classForReflectionService
) {

    override fun generateClassName(): String {
        return "NotificationServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {
        write("import ${JacksonClient::class.qualifiedName};")
        write("import ${SnsEventMapper::class.qualifiedName};")
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("${SNSEvent.SNS::class.java.canonicalName} snsEvent = input.getRecords().get(0).getSNS();")

        if (eventParam.exists()) {
            write("$eventSimpleName event = ${SnsEventMapper.javaClass.simpleName}.getNotificationEvent(snsEvent, requestId);")
        }

        if (inputParam.exists()) {
            write("${inputParam.simpleName()} parsedType = JacksonClient.readValue(snsEvent.getMessage(), ${inputParam.simpleName()}.class);")
        }

        val methodName = fileBuilderMethodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName(event);")
            eventParam.type == null -> write("handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("handler.$methodName(parsedType, event);")
            else -> write("handler.$methodName(event, parsedType);")
        }
        write("return null;")
    }
}
