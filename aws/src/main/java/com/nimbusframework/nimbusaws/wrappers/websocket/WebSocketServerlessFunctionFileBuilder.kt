package com.nimbusframework.nimbusaws.wrappers.websocket

import com.nimbusframework.nimbusaws.clients.AwsInternalClientBuilder
import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.clients.ClientBinder
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketResponse
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        WebSocketServerlessFunction::class.java.simpleName,
        WebSocketEvent::class.java,
        compilingElement,
        null,
        null,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "WebSocketServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import com.fasterxml.jackson.databind.DeserializationFeature;")
        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")

        write("import ${ClientBinder::class.qualifiedName};")
        write("import ${AwsInternalClientBuilder::class.qualifiedName};")
        write("import ${WebSocketEvent::class.qualifiedName};")
        write("import ${WebSocketResponse::class.qualifiedName};")

        if (methodInformation.packageName.isNotBlank()) {
            write("import ${methodInformation.packageName}.${methodInformation.className};")
        }
    }

    private fun writeInputs(param: Param) {
        write("ClientBinder.INSTANCE.setInternalBuilder(AwsInternalClientBuilder.INSTANCE);")
        write("WebSocketEvent event = objectMapper.readValue(jsonString, WebSocketEvent.class);")
        write("event.setRequestId(context.getAwsRequestId());")

        if (param.type != null) {
            write("String body = event.getBody();")
            write("${param.type} parsedType;")
            write("try {")
            write("objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);")
            write("parsedType = objectMapper.readValue(body, ${param.type}.class);")
            write("} catch (Exception e) {")
            write("e.printStackTrace();")
            write("WebSocketResponse response = new WebSocketResponse(500);")
            write("String responseString = objectMapper.writeValueAsString(response);")
            write("PrintWriter writer = new PrintWriter(output);")
            write("writer.print(responseString);")
            write("writer.close();")
            write("output.close();")
            write("return;")
            write("}")
        }

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        val callPrefix = if (methodInformation.returnType.toString() == "void") {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        val methodName = methodInformation.methodName
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
        write("String responseString = objectMapper.writeValueAsString(response);")
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

                val className = getGeneratedClassName()
                val builderFile = processingEnv.filer.createSourceFile(className)

                out = PrintWriter(builderFile.openWriter())

                val packageName = findPackageName(methodInformation.packageName)

                if (packageName != "") write("package $packageName;")

                writeImports()

                write("public class ${getGeneratedClassName()} {")
                write("public void handleRequest(InputStream input, OutputStream output, Context context) {")

                write("ObjectMapper objectMapper = new ObjectMapper();")
                write("try {")

                write("String jsonString = new BufferedReader(new InputStreamReader(input)).lines().collect(Collectors.joining(\"\\n\"));")

                writeInputs(params.inputParam)

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

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