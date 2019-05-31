package com.nimbusframework.nimbuscore.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpEvent
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpResponse
import java.util.HashMap
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
        HttpEvent::class.java,
        compilingElement,
        APIGatewayProxyRequestEvent::class.java,
        APIGatewayProxyResponseEvent::class.java,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "HttpServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import ${HashMap::class.qualifiedName};")
        write("import ${HttpResponse::class.qualifiedName};")

        write()
    }


    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("ObjectMapper objectMapper = new ObjectMapper();")
        write("${HttpEvent::class.simpleName} event = new ${HttpEvent::class.simpleName}(input, requestId);")
        if (inputParam.exists()) {
            write("${inputParam.simpleName()} parsedType = objectMapper.readValue(input.getBody(), ${inputParam.simpleName()}.class);")
        }

        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName();")
            inputParam.doesNotExist() -> write("${callPrefix}handler.$methodName(event);")
            eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("${callPrefix}handler.$methodName(parsedType, event);")
            else -> write("${callPrefix}handler.$methodName(event, parsedType);")
        }

        write("APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent().withStatusCode(200);")

        if (methodInformation.returnType.toString() == HttpResponse::class.qualifiedName) {
            write("responseEvent.setBody(result.getBody());")
            write("responseEvent.setHeaders(result.getHeaders());")
            write("responseEvent.setStatusCode(result.getStatusCode());")
            write("responseEvent.setIsBase64Encoded(result.getIsBase64Encoded())")
        } else if (!voidMethodReturn) {
            write("String responseBody = objectMapper.writeValueAsString(result);")
            write("responseEvent.setBody(responseBody);")
        }
        addCorsHeader("responseEvent")
        write("return responseEvent;")
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent().withStatusCode(500);")
        addCorsHeader("errorResponse")
        write("return errorResponse;")
    }

    private fun addCorsHeader(variableName: String) {
        write("if ($variableName.getHeaders() == null) {")
        write("$variableName.setHeaders(new HashMap<>());")
        write("}")
        write("if (!$variableName.getHeaders() .containsKey(\"Access-Control-Allow-Origin\")) {")
        write("String allowedCorsOrigin = System.getenv(\"NIMBUS_ALLOWED_CORS_ORIGIN\");")
        write("if (allowedCorsOrigin != null && !allowedCorsOrigin.equals(\"\")) {")
        write("$variableName.getHeaders().put(\"Access-Control-Allow-Origin\", allowedCorsOrigin);")
        write("}")
        write("}")
    }
}