package wrappers.http

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.QueueServerlessFunction
import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.http.models.HttpEvent
import wrappers.http.models.LambdaProxyResponse
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class HttpServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        HttpServerlessFunction::class.java.simpleName,
        QueueServerlessFunction::class.java.simpleName,
        compilingElement
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
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${HttpEvent::class.qualifiedName};")
        write("import ${LambdaProxyResponse::class.qualifiedName};")

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
            write("LambdaProxyResponse response = new LambdaProxyResponse().withStatusCode(500).withMessage(\"JSON parsing error\");")
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
        if (methodInformation.returnType.toString() != LambdaProxyResponse::class.qualifiedName) {
            write("LambdaProxyResponse response = new LambdaProxyResponse();")

            if (methodInformation.returnType.toString() != "void") {
                write("String resultString = objectMapper.writeValueAsString(result);")
                write("response.setBody(resultString);")
            }
        } else {
            write("LambdaProxyResponse response = result;")
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
        write("LambdaProxyResponse errorResponse = LambdaProxyResponse.Companion.serverErrorResponse();")
        write("String responseString = objectMapper.writeValueAsString(errorResponse);")

        write("PrintWriter writer = new PrintWriter(output);")
        write("writer.print(responseString);")
        write("writer.close();")
        write("output.close();")

        write("} catch (IOException e2) {")
        write("e2.printStackTrace();")
        write("}")
    }
}