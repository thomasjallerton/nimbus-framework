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
    private val CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY = "NIMBUS_CUSTOM_RUNTIME_FUNCTION_IDENTIFIER"

    fun createCustomRuntimeEntryFunction(initialFunctions: List<FunctionInformation>) {

        // Add the env variable to all functions, and then only process once of each in the file.
        val functions = initialFunctions.filter { it.awsMethodInformation != null }.groupBy { it.awsMethodInformation!!.packageName + it.awsMethodInformation.generatedClassName }.map { (identifier, functions) ->
            functions.forEach {
                it.resource.addEnvVariable(CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY, identifier)
            }
            Pair(identifier, functions.first())
        }

        val builderFile = processingEnv.filer.createSourceFile(CUSTOM_RUNTIME_ENTRY_CLASS_NAME)
        out = PrintWriter(builderFile.openWriter())

        import(CustomRuntimeHandler::class.java)
        for ((_, function) in functions) {
            if (function.awsMethodInformation!!.packageName != "") {
                import(function.awsMethodInformation.packageName + "." + function.awsMethodInformation.generatedClassName)
            }
        }
        import(IOException::class.java)
        import(InterruptedException::class.java)
        import(IllegalStateException::class.java)
        import(ObjectMapper::class.java)
        import(DeserializationFeature::class.java)
        import(InvocationResponse::class.java)
        import(LambdaExecutionFunction::class.java)
        import(CustomContext::class.java)
        write("public class $CUSTOM_RUNTIME_ENTRY_CLASS_NAME {")
        write()
        write("public static void main(String[] args) throws IOException, InterruptedException {")
        write("ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);")
        write("CustomRuntimeHandler customRuntimeHandler = new CustomRuntimeHandler(objectMapper);")
        write("String endpointIdentifier = System.getenv(\"${CUSTOM_RUNTIME_FUNCTION_IDENTIFIER_ENV_KEY}\");")
        write("LambdaExecutionFunction handler = getConsumer(endpointIdentifier, objectMapper, customRuntimeHandler);")
        write("while (true) {")
        write("String endpoint = System.getenv(\"AWS_LAMBDA_RUNTIME_API\");")
        write("InvocationResponse invocation = customRuntimeHandler.getInvocation(endpoint);")
        write("CustomContext context = new CustomContext(invocation.getRequestId());")
        write("try {")
        write("handler.accept(invocation, context);")
        write("} catch (Exception e) {")
        write("e.printStackTrace();")
        write("customRuntimeHandler.handleException(invocation, e);")
        write("}") // try - catch
        write("}") // while true
        write("}") // main
        writeGetConsumerFunction(functions)
        write("}") // class
        out?.close()
    }

    private fun handleFunction(functionInformation: FunctionInformation, identifier: String) {

        write("case \"$identifier\":")
        val (_, className, inputType, returnType) = functionInformation.awsMethodInformation!!
        write("\t$className ${className.toLowerCase()} = new ${className}();")

        write("\treturn (invocation, context) -> {")
        write("\t$inputType request = objectMapper.readValue(invocation.getEvent(), ${inputType}.class);")
        write("\t$returnType response = ${className.toLowerCase()}.handleRequest(request, context);")
        write("\tcustomRuntimeHandler.sendResponse(response, invocation);")
        write("\t};")
    }

    private fun writeGetConsumerFunction(functions: List<Pair<String, FunctionInformation>>) {
        write("public static LambdaExecutionFunction getConsumer(String endpointIdentifier, ObjectMapper objectMapper, CustomRuntimeHandler customRuntimeHandler) {")
        write("switch (endpointIdentifier) {")
        for ((identifier, function) in functions) {
            handleFunction(function, identifier)
        }
        write("}") // switch
        write("throw new IllegalStateException(\"Unknown lambda method, missing function identifier env variable, or not configured to use a custom runtime\");")
        write("}") // function
    }


}
