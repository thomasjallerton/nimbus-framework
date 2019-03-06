package wrappers.deployment

import annotation.annotations.deployment.AfterDeployment
import cloudformation.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class DeploymentFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        AfterDeployment::class.java.simpleName,
        null,
        compilingElement
) {
    override fun getGeneratedClassName(): String {
        return "AfterDeployment${methodInformation.className}${methodInformation.methodName}"
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

        write()
    }

    override fun writeInputs(param: ServerlessFunctionFileBuilder.Param){}

    override fun writeFunction(inputParam: ServerlessFunctionFileBuilder.Param, eventParam: ServerlessFunctionFileBuilder.Param) {
        val callPrefix = if (methodInformation.returnType.toString() == "void") {
            ""
        } else {
            "${methodInformation.returnType} result = "
        }

        write("${callPrefix}handler.${methodInformation.methodName}();")
    }

    override fun writeOutput() {
        if (methodInformation.returnType.toString() != "void") {
            write("String resultString = objectMapper.writeValueAsString(result);")
            write("PrintWriter writer = new PrintWriter(output);")
            write("writer.print(resultString);")
            write("writer.close();")
        }
        write("output.close();")
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }

    override fun isValidFunction(functionParams: ServerlessFunctionFileBuilder.FunctionParams) {
        if (methodInformation.parameters.isNotEmpty()) {
            compilationError("Too many parameters for AfterDeployment, can have none")
        }
    }
}