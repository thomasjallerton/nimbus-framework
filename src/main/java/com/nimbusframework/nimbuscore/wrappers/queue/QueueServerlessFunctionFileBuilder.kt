package com.nimbusframework.nimbuscore.wrappers.queue

import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.queue.models.QueueEvent
import com.nimbusframework.nimbuscore.wrappers.queue.models.RecordCollection
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class QueueServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState

) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        QueueServerlessFunction::class.java.simpleName,
        QueueEvent(),
        compilingElement,
        nimbusState
) {

    override fun writeOutput() {}

    override fun getGeneratedClassName(): String {
        return "QueueServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun eventCannotBeList(): Boolean {
        return false
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        write("import java.util.List;")
        write("import java.util.LinkedList;")
        if (methodInformation.packageName.isNotBlank()) {
            write("import ${methodInformation.packageName}.${methodInformation.className};")
        }
        write("import ${QueueEvent::class.qualifiedName};")
        write("import ${RecordCollection::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {

        write("RecordCollection records = objectMapper.readValue(jsonString, RecordCollection.class);")

        if (param.type != null) {
            write("List<QueueEvent> events = records.getRecords();")

            if (isAListType(param.type)) {
                write("${param.type} parsedType = new LinkedList();")
                write("for (QueueEvent event : events) {")
                write("parsedType.add(objectMapper.readValue(event.getBody(), ${findListType(param.type)}.class));")
                write("}")
            } else {
                write("for (QueueEvent event : events) {")
                write("${param.type} parsedType = objectMapper.readValue(event.getBody(), ${param.type}.class);")
            }
        }

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val eventVariable = if (eventParam.type != null && !isAListType(eventParam.type)) {
            "event"
        } else {
            "events"
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.isEmpty() && eventParam.isEmpty() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName($eventVariable);")
            eventParam.type == null -> write("handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("handler.$methodName(parsedType, $eventVariable);")
            else -> write("handler.$methodName($eventVariable, parsedType);")
        }

        if (inputParam.type != null && !isAListType(inputParam.type)) {
            write("}")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}