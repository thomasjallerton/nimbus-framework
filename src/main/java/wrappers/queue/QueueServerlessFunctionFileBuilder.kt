package wrappers.queue

import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.queue.models.QueueEvent
import wrappers.queue.models.RecordCollection
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class QueueServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation
) : ServerlessFunctionFileBuilder(processingEnv, methodInformation) {

    override fun writeOutput() {}

    override fun isValidFunction() {
        if (methodInformation.parameters.size > 2) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Not a valid queue function handler (too many arguments)")
        }
    }

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

    override fun writeInputs(inputParam: InputParam) {

        write("RecordCollection records = objectMapper.readValue(jsonString, RecordCollection.class);")

        if (inputParam.type != null) {
            write("List<QueueEvent> events = records.getRecords();")

            if (isAListType(inputParam.type)) {
                write("${inputParam.type} parsedType = new LinkedList();")
                write("for (QueueEvent event : events) {")
                write("parsedType.add(objectMapper.readValue(event.getBody(), ${findListType(inputParam.type)}.class));")
                write("}")
            } else {
                write("for (QueueEvent event : events) {")
                write("${inputParam.type} parsedType = objectMapper.readValue(event.getBody(), ${inputParam.type}.class);")
            }
        }

    }

    override fun writeFunction(inputParam: InputParam) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        val eventVariable = if (inputParam.type != null && !isAListType(inputParam.type)) {
            "event"
        } else {
            "events"
        }

        when {
            methodInformation.parameters.size == 1 -> write("handler.${methodInformation.methodName}($eventVariable);")
            inputParam.index == 0 -> write("handler.${methodInformation.methodName}(parsedType, $eventVariable);")
            else -> write("handler.${methodInformation.methodName}($eventVariable, parsedType);")
        }

        if (inputParam.type != null && !isAListType(inputParam.type)) {
            write("}")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}