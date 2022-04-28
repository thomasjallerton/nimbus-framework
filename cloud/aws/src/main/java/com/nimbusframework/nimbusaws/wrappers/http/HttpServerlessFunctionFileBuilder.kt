package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.annotations.function.HttpException
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import com.nimbusframework.nimbuscore.eventabstractions.HttpResponse
import java.util.HashMap
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class HttpServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    HttpServerlessFunction::class.java.simpleName,
    HttpEvent::class.java,
    compilingElement,
    APIGatewayProxyRequestEvent::class.java,
    APIGatewayProxyResponseEvent::class.java,
    classForReflectionService
) {

    override fun generateClassName(): String {
        return "HttpServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {
        write()

        write("import ${JacksonClient::class.qualifiedName};")
        write("import ${HashMap::class.qualifiedName};")
        write("import ${HttpResponse::class.qualifiedName};")
        write("import ${RestApiGatewayEventMapper::class.qualifiedName};")
        write("import ${HttpException::class.qualifiedName};")

        write()
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("${HttpEvent::class.simpleName} event = ${RestApiGatewayEventMapper::class.simpleName}.getHttpEvent(input, requestId);")
        if (inputParam.exists()) {
            write("${inputParam.canonicalName()} parsedType = JacksonClient.readValue(input.getBody(), ${inputParam.simpleName()}.class);")
        }

        val callPrefix = if (voidMethodReturn) {
            ""
        } else {
            "${fileBuilderMethodInformation.returnType} result = "
        }

        val methodName = fileBuilderMethodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName();")
            inputParam.doesNotExist() -> write("${callPrefix}handler.$methodName(event);")
            eventParam.doesNotExist() -> write("${callPrefix}handler.$methodName(parsedType);")
            inputParam.index == 0 -> write("${callPrefix}handler.$methodName(parsedType, event);")
            else -> write("${callPrefix}handler.$methodName(event, parsedType);")
        }

        write("APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent().withStatusCode(200);")

        if (fileBuilderMethodInformation.returnType.toString() == HttpResponse::class.qualifiedName) {
            write("responseEvent.setBody(result.getBody());")
            write("responseEvent.setHeaders(result.getHeaders());")
            write("responseEvent.setStatusCode(result.getStatusCode());")
            write("responseEvent.setIsBase64Encoded(result.isBase64Encoded());")
        } else if (!voidMethodReturn) {
            write("String responseBody = JacksonClient.writeValueAsString(result);")
            write("responseEvent.setBody(responseBody);")
        }
        addCorsHeader("responseEvent")
        write("return responseEvent;")
    }

    override fun writeCustomExceptionHandler() {
        write("} catch (HttpException e) {")
        write("APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent()")
        write("\t.withStatusCode(e.getStatusCode())")
        write("\t.withBody(e.getMessage());")
        addCorsHeader("errorResponse")
        write("return errorResponse;")
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
        write("if (!$variableName.getHeaders().containsKey(\"Access-Control-Allow-Origin\")) {")
        write("String allowedCorsOrigin = System.getenv(\"${NimbusConstants.allowedOriginEnvVariable}\");")
        write("if (allowedCorsOrigin != null && !allowedCorsOrigin.equals(\"\")) {")
        write("$variableName.getHeaders().put(\"Access-Control-Allow-Origin\", allowedCorsOrigin);")
        write("}")
        write("}")
    }
}
