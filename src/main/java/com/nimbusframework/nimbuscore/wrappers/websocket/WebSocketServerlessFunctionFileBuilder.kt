package com.nimbusframework.nimbuscore.wrappers.websocket

import com.nimbusframework.nimbuscore.annotation.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.websocket.models.WebSocketEvent
import com.nimbusframework.nimbuscore.wrappers.websocket.models.WebSocketResponse
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
        WebSocketEvent(),
        compilingElement,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "WebSocketServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.DeserializationFeature;")
        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.packageName.isNotBlank()) {
            write("import ${methodInformation.packageName}.${methodInformation.className};")
        }
        write("import ${WebSocketEvent::class.qualifiedName};")
        write("import ${WebSocketResponse::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {

        write("System.out.println(jsonString);")

        write("WebSocketEvent event = objectMapper.readValue(jsonString, WebSocketEvent.class);")


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
            inputParam.isEmpty() && eventParam.isEmpty() -> write("${callPrefix}handler.$methodName();")
            inputParam.type == null -> write("${callPrefix}handler.$methodName(event);")
            eventParam.type == null -> write("${callPrefix}handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("${callPrefix}handler.$methodName(parsedType, event);")
            else -> write("${callPrefix}handler.$methodName(event, parsedType);")
        }
    }

    override fun writeOutput() {
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
}