package wrappers.queue

import annotation.annotations.function.QueueServerlessFunction
import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.queue.models.QueueEvent
import wrappers.queue.models.RecordCollection
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class QueueServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        QueueServerlessFunction::class.java.simpleName,
        QueueEvent::class.java.simpleName,
        compilingElement
) {

    override fun writeOutput() {}

    override fun getGeneratedClassName(): String {
        return "QueueServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        write("import java.util.List;")
        write("import java.util.LinkedList;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
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
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        val eventVariable = if (inputParam.type != null && !isAListType(inputParam.type)) {
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