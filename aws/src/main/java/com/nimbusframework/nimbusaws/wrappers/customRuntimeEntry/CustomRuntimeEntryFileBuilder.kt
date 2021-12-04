package com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.wrappers.FileBuilder
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment

class CustomRuntimeEntryFileBuilder(
    private val processingEnv: ProcessingEnvironment
): FileBuilder() {

    private val CUSTOM_RUNTIME_ENTRY_CLASS_NAME: String = "CustomLambdaRuntimeEntry"
    private val CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY = "CUSTOM_RUNTIME_FUNCTION_IDENTIFIER"

    private var alreadyWritten = false

    fun createCustomRuntimeEntryFunction(functions: List<FunctionInformation>) {
        if (alreadyWritten) return
        alreadyWritten = true

        val builderFile = processingEnv.filer.createSourceFile(CUSTOM_RUNTIME_ENTRY_CLASS_NAME)
        out = PrintWriter(builderFile.openWriter())

        import(CustomRuntimeHandler::class.java)
        for (function in functions) {
            if (function.awsMethodInformation.packageName != "") {
                import(function.awsMethodInformation.packageName + "." + function.awsMethodInformation.generatedClassName)
            }
        }
        import(IOException::class.java)
        import(InterruptedException::class.java)
        import(ObjectMapper::class.java)
        import(DeserializationFeature::class.java)
        import(InvocationResponse::class.java)
        import(CustomContext::class.java)
        write("public class $CUSTOM_RUNTIME_ENTRY_CLASS_NAME {")
        write()
        write("public static void main(String[] args) throws IOException, InterruptedException {")
        write("ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);")
        write("CustomRuntimeHandler customRuntimeHandler = new CustomRuntimeHandler(objectMapper);")
        for (function in functions) {
            val className = function.awsMethodInformation.generatedClassName
            write("$className ${className.toLowerCase()} = new ${className}();")
        }
        write("String endpointIdentifier = System.getenv(\"${CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY}\");")
        write("while (true) {")
        write("String endpoint = System.getenv(\"AWS_LAMBDA_RUNTIME_API\");")
        write("InvocationResponse invocation = customRuntimeHandler.getInvocation(endpoint);")
        write("CustomContext context = new CustomContext(invocation.getRequestId());")
        write("try {")
        write("switch (endpointIdentifier) {")
        for ((index, function) in functions.withIndex()) {
            handleFunction(function, index.toString())
        }
        write("}") // switch
        write("} catch (Exception e) {")
        write("e.printStackTrace();")
        write("customRuntimeHandler.handleException(endpoint, invocation, e);")
        write("}") // try - catch
        write("}") // while true
        write("}") // main
        write("}") // class
        out?.close()
    }

    private fun handleFunction(functionInformation: FunctionInformation, identifier: String) {
        functionInformation.resource.addEnvVariable(CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY, identifier)

        write("case \"$identifier\":")

        val (_, className, inputType, returnType) = functionInformation.awsMethodInformation
        write("$inputType request = objectMapper.readValue(invocation.getEvent(), ${inputType}.class);")
        write("$returnType response = ${className.toLowerCase()}.handleRequest(request, context);")
        write("customRuntimeHandler.sendResponse(response, endpoint, invocation);")

        write("break;")
    }


}
