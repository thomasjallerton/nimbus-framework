package wrappers.http

import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.http.models.HttpEvent
import wrappers.http.models.LambdaProxyResponse
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class HttpServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation
): ServerlessFunctionFileBuilder(processingEnv, methodInformation) {

    override fun getGeneratedClassName(): String {
        return "HttpServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun isValidFunction() {
        if (methodInformation.parameters.size > 2) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Not a valid http function handler (too many arguments)")
        }
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

    override fun writeInputs(inputParam: InputParam) {

        write("HttpEvent event = objectMapper.readValue(jsonString, HttpEvent.class);")

        if (inputParam.type != null) {
            write("String body = event.getBody();")
            write("${inputParam.type} parsedType = objectMapper.readValue(body, ${inputParam.type}.class);")
        }

    }

    override fun writeFunction(inputParam: InputParam) {
        val callPrefix = if (methodInformation.returnType.toString() == "void") {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        when {
            inputParam.type == null -> write("${callPrefix}handler.${methodInformation.methodName}(event);")
            inputParam.index == 0 -> write("${callPrefix}handler.${methodInformation.methodName}(parsedType, event);")
            else -> write("${callPrefix}handler.${methodInformation.methodName}(event, parsedType);")
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