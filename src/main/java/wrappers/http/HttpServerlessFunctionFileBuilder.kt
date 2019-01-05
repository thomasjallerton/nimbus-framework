package wrappers.http

import annotation.models.processing.MethodInformation
import wrappers.http.models.Event
import wrappers.http.models.LambdaProxyResponse
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

class HttpServerlessFunctionFileBuilder(
        private val processingEnv: ProcessingEnvironment,
        private val methodInformation: MethodInformation,
        private val messager: Messager
) {

    private var tabLevel: Int = 0

    private var out: PrintWriter? = null

    fun getHandler(): String {
        return if (customFunction()) {
            if (methodInformation.qualifiedName == "") {
                "${methodInformation.className}::${methodInformation.methodName}"
            } else {
                "${methodInformation.qualifiedName}.${methodInformation.className}::${methodInformation.methodName}"
            }
        } else {
            "${getGeneratedClassName()}::nimbusHandle"
        }
    }

    private fun getGeneratedClassName(): String {
        return "HttpServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    fun createClass() {
        if (!customFunction()) {
            try {

                if (methodInformation.parameters.size > 2) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Not a valid http function handler (too many arguments)")
                }

                val inputParam = findInputTypeAndIndex()

                val builderFile = processingEnv.filer.createSourceFile(getGeneratedClassName())
                out = PrintWriter(builderFile.openWriter())

                val packageName = findPackageName(methodInformation.qualifiedName)

                if (packageName != "") write("package $packageName;")

                writeImports()

                write("public class HttpServerlessFunction${methodInformation.className}${methodInformation.methodName} {")

                write()

                write("public void nimbusHandle(InputStream input, OutputStream output, Context context) {")

                write("ObjectMapper objectMapper = new ObjectMapper();")
                write("try {")

                writeInputs(inputParam)

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

                writeFunction(inputParam)

                writeOutput()

                write("} catch (Exception e) {")

                writeHandleError()

                write("}")
                write("return;")


                write("}")

                write("}")

                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun write(toWrite: String = "") {
        if (toWrite.startsWith("}")) tabLevel--

        var tabs = ""
        for (i in 1..tabLevel) {
            tabs += "\t"
        }
        out?.println("$tabs$toWrite")

        if (toWrite.endsWith("{")) tabLevel++
    }

    private fun findPackageName(qualifiedName: String): String {
        val lastDot = qualifiedName.lastIndexOf('.')
        return if (lastDot > 0) {
            qualifiedName.substring(0, lastDot)
        } else {
            ""
        }
    }

    private fun customFunction(): Boolean {
        val params = methodInformation.parameters
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

    private fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${Event::class.qualifiedName};")
        write("import ${LambdaProxyResponse::class.qualifiedName};")

        write()
    }

    private fun findInputTypeAndIndex(): InputParam {
        var inputParamIndex = 0
        for (param in methodInformation.parameters) {
            if (param.toString() != "wrappers.http.models.Event") {
                return InputParam(param, inputParamIndex)
            } else {
                inputParamIndex++
            }
        }
        return InputParam(null, 0)
    }

    private data class InputParam(val type: TypeMirror?, val index: Int)

    private fun writeInputs(inputParam: InputParam) {

        write("Event event = objectMapper.readValue(input, Event.class);")

        if (inputParam.type != null) {
            write("String body = event.getBody();")
            write("${inputParam.type} parsedType = objectMapper.readValue(body, ${inputParam.type}.class);")
        }

    }

    private fun writeFunction(inputParam: InputParam) {
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

    private fun writeOutput() {
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

    private fun writeHandleError() {
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