package com.nimbusframework.nimbusaws.wrappers.queue

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class QueueServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    QueueServerlessFunction::class.java.simpleName,
    QueueEvent::class.java,
    compilingElement,
    SQSEvent::class.java,
    Void::class.java,
    classForReflectionService
) {

    override fun generateClassName(): String {
        return "QueueServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun eventCannotBeList(): Boolean {
        return false
    }

    override fun writeImports() {
        write()

        write("import ${JacksonClient::class.qualifiedName};")
        write("import ${SqsEventMapper::class.qualifiedName};")
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


        write("List<SQSEvent.SQSMessage> records = input.getRecords();")

        if (isAListFunction) {
            write("List<$eventSimpleName> parsedRecords = new LinkedList<>();")
            write("List<$inputString> parsedTypes = new LinkedList<>();")
        }

        write("for (SQSEvent.SQSMessage message : records) {")
        if (isAListFunction) {
            write("parsedRecords.add(${SqsEventMapper::class.java.simpleName}.getQueueEvent(message, requestId));")
            write("parsedTypes.add(JacksonClient.readValue(message.getBody(), $inputString.class));")
        } else {
            write("$eventSimpleName event = ${SqsEventMapper::class.java.simpleName}.getQueueEvent(message, requestId);")
            write("$inputString parsedType = JacksonClient.readValue(message.getBody(), $inputString.class);")
            invokeHandler(inputParam, eventParam, "parsedType", "event")
        }
        write("}")

        if (isAListFunction) {
            invokeHandler(inputParam, eventParam, "parsedTypes", "parsedRecords")
        }
        write("return null;")
    }

    private fun invokeHandler(inputParam: Param, eventParam: Param, inputVariable: String, eventVariable: String) {
        val methodName = fileBuilderMethodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("handler.$methodName();")
            inputParam.doesNotExist() -> write("handler.$methodName($eventVariable);")
            eventParam.doesNotExist() -> write("handler.$methodName($inputVariable);")
            inputParam.index == 0 -> write("handler.$methodName($inputVariable, $eventVariable);")
            else -> write("handler.$methodName($eventVariable, $inputVariable);")
        }
    }
}
