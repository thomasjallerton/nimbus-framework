package wrappers.notification

import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.notification.models.NotificationEvent
import wrappers.notification.models.RecordCollection
import wrappers.notification.models.SnsMessageFormat
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class NotificationServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation
): ServerlessFunctionFileBuilder(processingEnv, methodInformation) {
    override fun writeOutput() {}

    override fun isValidFunction() {
        if (methodInformation.parameters.size > 2) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Not a valid notification function handler (too many arguments)")
        }
    }

    override fun getGeneratedClassName(): String {
        return "NotificationServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${NotificationEvent::class.qualifiedName};")
        write("import ${RecordCollection::class.qualifiedName};")
        write("import ${SnsMessageFormat::class.qualifiedName};")

        write()
    }

    override fun writeInputs(inputParam: InputParam) {

        write("RecordCollection records = objectMapper.readValue(jsonString, RecordCollection.class);")

        if (inputParam.type != null) {
            write("NotificationEvent event = records.getRecords().get(0).getSns();")
            write("SnsMessageFormat snsFormat = objectMapper.readValue(event.getMessage(), SnsMessageFormat.class);")
            write("${inputParam.type} parsedType;")
            write("if (snsFormat.getLambda() != null) {")
            write("parsedType = objectMapper.readValue(snsFormat.getLambda(), ${inputParam.type}.class);")
            write("} else if (snsFormat.getDefault() != null) {")
            write("parsedType = objectMapper.readValue(snsFormat.getDefault(), ${inputParam.type}.class);")
            write("} else {")
            write("return;")
            write("}")
        }

    }

    override fun writeFunction(inputParam: InputParam) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        when {
            methodInformation.parameters.size == 1 -> write("handler.${methodInformation.methodName}(event);")
            inputParam.index == 0 -> write("handler.${methodInformation.methodName}(parsedType, event);")
            else -> write("handler.${methodInformation.methodName}(event, parsedType);")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}