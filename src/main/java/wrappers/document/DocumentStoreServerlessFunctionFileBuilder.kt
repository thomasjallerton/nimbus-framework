package wrappers.document

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.models.processing.MethodInformation
import clients.dynamo.DynamoStreamParser
import wrappers.ServerlessFunctionFileBuilder
import wrappers.document.models.DynamoRecords
import wrappers.document.models.DynamoUpdate
import wrappers.notification.models.NotificationEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class DocumentStoreServerlessFunctionFileBuilder (
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        DocumentStoreServerlessFunction::class.java.simpleName,
        NotificationEvent(),
        compilingElement
) {
    override fun getGeneratedClassName(): String {
        return "DocumentStoreServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeOutput() {}

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${DynamoRecords::class.qualifiedName};")
        write("import ${DynamoUpdate::class.qualifiedName};")
        write("import ${DynamoStreamParser::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {

        write("DynamoRecords records = objectMapper.readValue(jsonString, DynamoRecords.class);")

        if (param.type != null) {
            write("DynamoUpdate update = records.getRecord().get(0).getDynamoDb();")
            write("DynamoStreamParser<${param.type}> parser = new DynamoStreamParser(${param.type}.class);")
            write("${param.type} parsedType = parser.toObject(update.getNewImage());")
        }

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.isEmpty() && eventParam.isEmpty() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName(event);")
            eventParam.type == null -> write("handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("handler.$methodName(parsedType, event);")
            else -> write("handler.$methodName(event, parsedType);")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}