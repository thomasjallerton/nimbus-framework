package com.nimbusframework.nimbuscore.wrappers.notification

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.notification.models.NotificationEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class NotificationServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        NotificationServerlessFunction::class.java.simpleName,
        NotificationEvent::class.java,
        compilingElement,
        SNSEvent::class.java,
        Void::class.java,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "NotificationServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write("import ${ObjectMapper::class.qualifiedName};")
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("ObjectMapper objectMapper = new ObjectMapper();")
        write("${SNSEvent.SNS::class.java.canonicalName} snsEvent = input.getRecords().get(0).getSNS();")

        if (eventParam.exists()) {
            write("$eventSimpleName event = new $eventSimpleName(snsEvent, requestId);")
        }

        if (inputParam.exists()) {
            write("${inputParam.simpleName()} parsedType = objectMapper.readValue(snsEvent.getMessage(), ${inputParam.simpleName()}.class);")
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName(event);")
            eventParam.type == null -> write("handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("handler.$methodName(parsedType, event);")
            else -> write("handler.$methodName(event, parsedType);")
        }
        write("return null;")
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }
}