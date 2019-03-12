package wrappers.file

import annotation.annotations.function.FileStorageServerlessFunction
import cloudformation.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.file.models.FileRecords
import wrappers.file.models.FileStorageEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class FileStorageServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        FileStorageServerlessFunction::class.java.simpleName,
        FileStorageEvent(),
        compilingElement
) {


    override fun writeOutput() {}

    override fun getGeneratedClassName(): String {
        return "FileStorageServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.packageName.isNotBlank()) {
            write("import ${methodInformation.packageName}.${methodInformation.className};")
        }
        write("import ${FileStorageEvent::class.qualifiedName};")
        write("import ${FileRecords::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {
        write("FileRecords records = objectMapper.readValue(jsonString, FileRecords.class);")
        write("FileStorageEvent event = records.getRecords().get(0).getS3().getObj();")
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        val methodName = methodInformation.methodName
        when {
            eventParam.isEmpty() -> write("handler.$methodName();")
            else -> write("handler.$methodName(event);")
        }
    }

    override fun isValidFunction(functionParams: ServerlessFunctionFileBuilder.FunctionParams) {
        if (functionParams.inputParam.index != -1) {
            compilationError("FileStorageServerlessFunction cannot have a data input type, only a maximum of one FileStorageEvent type")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}