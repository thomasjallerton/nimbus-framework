package com.nimbusframework.nimbuscore.wrappers.http

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpEvent
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpResponse
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class HttpServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        HttpServerlessFunction::class.java.simpleName,
        HttpEvent(),
        compilingElement,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "HttpServerlessFunction${methodInformation.className}${methodInformation.methodName}"
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
        write("import ${HttpEvent::class.qualifiedName};")
        write("import ${HttpResponse::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {

        write("HttpEvent event = objectMapper.readValue(jsonString, HttpEvent.class);")

        if (param.type != null) {
            write("String body = event.getBody();")
            write("${param.type} parsedType;")
            write("try {")
            write("parsedType = objectMapper.readValue(body, ${param.type}.class);")
            write("} catch (Exception e) {")
            write("e.printStackTrace();")
            write("HttpResponse response = new HttpResponse().withStatusCode(500).withBody(\"{\\\"message\\\":\\\"JSON parsing error\\\"}\");")
            addCorsHeader("response")
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
        if (methodInformation.returnType.toString() != HttpResponse::class.qualifiedName) {
            write("HttpResponse response = new HttpResponse();")
            addCorsHeader("response")
            if (methodInformation.returnType.toString() != "void") {
                write("String resultString = objectMapper.writeValueAsString(result);")
                write("response.setBody(resultString);")
            }
        } else {
            write("HttpResponse response = result;")
            addCorsHeader("response")
        }

        write("String responseString = objectMapper.writeValueAsString(response);")
        write("PrintWriter writer = new PrintWriter(output);")
        write("writer.print(responseString);")
        write("writer.close();")
        write("output.close();")
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")

        write("try {")
        write("HttpResponse errorResponse = HttpResponse.Companion.serverErrorResponse();")
        addCorsHeader("errorResponse")
        write("String responseString = objectMapper.writeValueAsString(errorResponse);")

        write("PrintWriter writer = new PrintWriter(output);")
        write("writer.print(responseString);")
        write("writer.close();")
        write("output.close();")

        write("} catch (IOException e2) {")
        write("e2.printStackTrace();")
        write("}")
    }

    private fun addCorsHeader(variableName: String) {
        write("if (!$variableName.getHeaders().containsKey(\"Access-Control-Allow-Origin\")) {")
        write("String allowedCorsOrigin = System.getenv(\"NIMBUS_ALLOWED_CORS_ORIGIN\");")
        write("if (allowedCorsOrigin != null && !allowedCorsOrigin.equals(\"\")) {")
        write("$variableName.getHeaders().put(\"Access-Control-Allow-Origin\", allowedCorsOrigin);")
        write("}")
        write("}")
    }
}