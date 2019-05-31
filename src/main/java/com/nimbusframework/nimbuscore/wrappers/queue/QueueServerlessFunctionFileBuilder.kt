package com.nimbusframework.nimbuscore.wrappers.queue

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.queue.models.QueueEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class QueueServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState

) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        QueueServerlessFunction::class.java.simpleName,
        QueueEvent::class.java,
        compilingElement,
        SQSEvent::class.java,
        Void::class.java,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "QueueServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun eventCannotBeList(): Boolean {
        return false
    }

    override fun writeImports() {
        write()

        write("import ${ObjectMapper::class.qualifiedName};")
        write("import java.util.List;")
        write("import java.util.LinkedList;")

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val isAListFunction = if (eventParam.exists()) {
            isAListType(eventParam.type!!)
        } else {
            if (inputParam.exists()) {
                isAListType(inputParam.type!!)
            } else {
                false
            }
        }

        val inputString = if (inputParam.exists()) {
            if (isAListType(inputParam.type!!)) {
                findListType(inputParam.type)
            } else {
                inputParam.simpleName()
            }
        } else {
            "java.lang.Void"
        }


        write("ObjectMapper objectMapper = new ObjectMapper();")

        write("List<SQSEvent.SQSMessage> records = input.getRecords();")

        if (isAListFunction) {
            write("List<$eventSimpleName> parsedRecords = new LinkedList<>();")
            write("List<$inputString> parsedTypes = new LinkedList<>();")
        }

        write("for (SQSEvent.SQSMessage message : records) {")
        if (isAListFunction) {
            write("parsedRecords.add(new $eventSimpleName(message, requestId));")
            write("parsedTypes.add(objectMapper.readValue(message.getBody(), $inputString.class));")
        } else {
            write("$eventSimpleName event = new $eventSimpleName(message, requestId);")
            write("$inputString parsedType = objectMapper.readValue(message.getBody(), $inputString.class);")
            invokeHandler(inputParam, eventParam, "parsedType", "event")
        }
        write("}")

        if (isAListFunction) {
            invokeHandler(inputParam, eventParam, "parsedTypes", "parsedRecords")
        }
        write("return null;")
    }

    private fun invokeHandler(inputParam: Param, eventParam: Param, inputVariable: String, eventVariable: String) {
        val methodName = methodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("handler.$methodName();")
            inputParam.doesNotExist() -> write("handler.$methodName($eventVariable);")
            eventParam.doesNotExist() -> write("handler.$methodName($inputVariable);")
            inputParam.index == 0 -> write("handler.$methodName($inputVariable, $eventVariable);")
            else -> write("handler.$methodName($eventVariable, $inputVariable);")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }
}