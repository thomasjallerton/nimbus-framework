package com.nimbusframework.nimbusaws.wrappers.http

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.NimbusConstants
import com.nimbusframework.nimbuscore.annotations.http.HttpException
import com.nimbusframework.nimbuscore.annotations.http.HttpUtils
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import com.nimbusframework.nimbuscore.eventabstractions.HttpResponse
import java.util.HashMap
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import java.util.Base64

class HttpServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService,
    private val enableRequestCompression: Boolean,
    private val enableResponseCompression: Boolean
) : ServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    HttpServerlessFunction::class.java.simpleName,
    HttpEvent::class.java,
    compilingElement,
    APIGatewayV2HTTPEvent::class.java,
    APIGatewayV2HTTPResponse::class.java,
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
        write("import ${HttpUtils::class.qualifiedName};")
        write("import ${Base64::class.qualifiedName};")

        write()
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("${HttpEvent::class.simpleName} event = ${RestApiGatewayEventMapper::class.simpleName}.getHttpEvent(input, requestId);")
        if (inputParam.exists()) {
            if (!enableRequestCompression) {
                write("String body = input.getBody();")
            } else {
                // Write code to detect compression
                write("String body = ${HttpUtils::class.simpleName}.getUncompressedContent(event);")
            }
            write("${inputParam.canonicalName()} parsedType = JacksonClient.readValue(body, ${inputParam.simpleName()}.class);")
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

        write("APIGatewayV2HTTPResponse responseEvent = new APIGatewayV2HTTPResponse();")
        write("responseEvent.setStatusCode(200);")

        val isByteArray = fileBuilderMethodInformation.returnType.toString() == "byte[]"

        if (fileBuilderMethodInformation.returnType.toString() == HttpResponse::class.qualifiedName) {
            if (enableResponseCompression) {
                error("enableResponseCompression cannot be used in HttpServerlessFunction if return type is ${HttpResponse::class.qualifiedName}")
            }
            write("responseEvent.setBody(result.getBody());")
            write("responseEvent.setHeaders(result.getHeaders());")
            write("responseEvent.setStatusCode(result.getStatusCode());")
            write("responseEvent.setIsBase64Encoded(result.isBase64Encoded());")
        } else if (!voidMethodReturn) {
            if (enableResponseCompression) {
                val httpUtilsClass = "${HttpUtils::class.simpleName}.${HttpUtils.CompressedContent::class.simpleName}"
                if (isByteArray) {
                    write("String responseBody;")
                    write("$httpUtilsClass compressedContent = ${HttpUtils::class.simpleName}.compressContent(event, result);")
                    write("responseEvent.setIsBase64Encoded(true);")
                    write("if (compressedContent != null) {")
                    write("responseBody = Base64.getEncoder().encodeToString(compressedContent.getContent());")
                    setHeader("responseEvent", "Content-Encoding", "compressedContent.getEncoding()")
                    write("} else {")
                    write("responseBody = Base64.getEncoder().encodeToString(result);")
                    write("}")
                } else {
                    write("String responseBody = JacksonClient.writeValueAsString(result);")
                    write("$httpUtilsClass compressedContent = ${HttpUtils::class.simpleName}.compressContent(event, responseBody);")
                    write("if (compressedContent != null) {")
                    write("responseBody = Base64.getEncoder().encodeToString(compressedContent.getContent());")
                    write("responseEvent.setIsBase64Encoded(true);")
                    setHeader("responseEvent", "Content-Encoding", "compressedContent.getEncoding()")
                    write("}")
                }
            } else if (isByteArray) {
                write("String responseBody = Base64.getEncoder().encodeToString(result);")
            } else {
                write("String responseBody = JacksonClient.writeValueAsString(result);")
            }
            write("responseEvent.setBody(responseBody);")
        }
        write("return responseEvent;")
    }

    override fun writeCustomExceptionHandler() {
        write("} catch (HttpException e) {")
        write("APIGatewayV2HTTPResponse errorResponse = new APIGatewayV2HTTPResponse();")
        write("errorResponse.setStatusCode(e.getStatusCode());")
        write("errorResponse.setBody(e.getMessage());")
        write("return errorResponse;")
    }


    private fun setHeader(variableName: String, header: String, valueVariable: String) {
        write("if ($variableName.getHeaders() == null) {")
        write("$variableName.setHeaders(new HashMap<>());")
        write("}")
        write("$variableName.getHeaders().put(\"$header\", $valueVariable);")
    }
}
