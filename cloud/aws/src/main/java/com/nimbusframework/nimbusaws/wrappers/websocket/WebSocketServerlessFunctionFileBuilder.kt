package com.nimbusframework.nimbusaws.wrappers.websocket

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.clients.AwsInternalClientBuilder
import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketResponse
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    WebSocketServerlessFunction::class.java.simpleName,
    WebSocketEvent::class.java,
    compilingElement,
    null,
    null,
    classForReflectionService
) {

    init {
        classForReflectionService.addClassForReflection(WebSocketEvent::class.java)
        classForReflectionService.addClassForReflection(WebSocketResponse::class.java)
    }

    override fun generateClassName(): String {
        return "WebSocketServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import com.fasterxml.jackson.databind.DeserializationFeature;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")

        write("import ${JacksonClient::class.qualifiedName};")
        write("import ${ClientBinder::class.qualifiedName};")
        write("import ${AwsInternalClientBuilder::class.qualifiedName};")
        write("import ${WebSocketEvent::class.qualifiedName};")
        write("import ${WebSocketResponse::class.qualifiedName};")

        if (fileBuilderMethodInformation.packageName.isNotBlank()) {
            write("import ${fileBuilderMethodInformation.packageName}.${fileBuilderMethodInformation.className};")
        }
    }

    private fun writeInputs(param: Param) {
        write("ClientBinder.INSTANCE.setInternalBuilder(AwsInternalClientBuilder.INSTANCE);")
        write("WebSocketEvent event = JacksonClient.readValue(jsonString, WebSocketEvent.class);")
        write("event.setRequestId(context.getAwsRequestId());")

        if (param.type != null) {
            write("String body = event.getBody();")
            write("${param.type} parsedType;")
            write("try {")
            write("parsedType = JacksonClient.readValue(body, ${param.type}.class);")
            write("} catch (Exception e) {")
            write("e.printStackTrace();")
            write("WebSocketResponse response = new WebSocketResponse(500);")
            write("String responseString = JacksonClient.writeValueAsString(response);")
            write("PrintWriter writer = new PrintWriter(output);")
            write("writer.print(responseString);")
            write("writer.close();")
            write("output.close();")
            write("return;")
            write("}")
        }

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val callPrefix = if (fileBuilderMethodInformation.returnType.toString() == "void") {
            ""
        } else {
            "${fileBuilderMethodInformation.returnType} result = "
        }

        val methodName = fileBuilderMethodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName();")
            inputParam.type == null -> write("${callPrefix}handler.$methodName(event);")
            eventParam.type == null -> write("${callPrefix}handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("${callPrefix}handler.$methodName(parsedType, event);")
            else -> write("${callPrefix}handler.$methodName(event, parsedType);")
        }
    }

    private fun writeOutput() {
        write("WebSocketResponse response = new WebSocketResponse(200);")
        write("String responseString = JacksonClient.writeValueAsString(response);")
        write("PrintWriter writer = new PrintWriter(output);")
        write("writer.print(responseString);")
        write("writer.close();")
        write("output.close();")
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }

    override fun createClass() {
        if (!customFunction()) {
            try {


                isValidFunction(params)

                val className = generatedClassName
                val builderFile = processingEnv.filer.createSourceFile(className)

                out = PrintWriter(builderFile.openWriter())

                val packageName = fileBuilderMethodInformation.packageName

                if (packageName != "") write("package $packageName;")

                writeImports()

                write("public class ${generatedClassName} {")
                write("public void handleRequest(InputStream input, OutputStream output, Context context) {")

                write("try {")

                write("String jsonString = new BufferedReader(new InputStreamReader(input)).lines().collect(Collectors.joining(\"\\n\"));")

                writeInputs(params.inputParam)

                write("${fileBuilderMethodInformation.className} handler = new ${fileBuilderMethodInformation.className}();")

                writeFunction(params.inputParam, params.eventParam)

                writeOutput()

                write("} catch (Exception e) {")

                writeHandleError()

                write("}")
                write("return;")


                write("}")

                write("}")

                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
