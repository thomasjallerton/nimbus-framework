package wrappers

import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

class HttpServerlessFunctionFileBuilder(
        private val processingEnv: ProcessingEnvironment,
        private val className: String,
        private val methodName: String,
        private val functionPath: String,
        private val params: List<TypeMirror>,
        private val messager: Messager
) {

    private var tabLevel: Int = 0

    private var out: PrintWriter? = null

    fun getHandler(): String {
        return if (customFunction()) {
            if (functionPath == "") {
                "$className::$methodName"
            } else {
                "$functionPath.$className::$methodName"
            }
        } else {
            "${getGeneratedClassName()}::nimbusHandle"
        }
    }

    private fun getGeneratedClassName(): String {
        return "HttpServerlessFunction$className$methodName"
    }

    fun createClass(qualifiedName: String, returnType: TypeMirror) {
        if (!customFunction()) {
            try {

                if (params.size > 2) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Not a valid http function handler (too many arguments)")
                }

                var inputParam: TypeMirror? = null
                var inputParamIndex = 0
                for (param in params) {
                    if (param.toString() != "wrappers.models.Event") {
                        inputParam = param
                        break
                    } else {
                        inputParamIndex++
                    }
                }

                val builderFile = processingEnv.filer
                        .createSourceFile(getGeneratedClassName())
                out = PrintWriter(builderFile.openWriter())

                val packageName = findPackageName(qualifiedName)

                if (packageName != "") write("package $packageName;")

                write()

                write("import com.fasterxml.jackson.databind.ObjectMapper;")
                write("import com.amazonaws.services.lambda.runtime.Context;")
                write("import java.io.*;")
                write("import java.util.stream.Collectors;")
                write("import $functionPath.$className;")
                write("import wrappers.models.Event;")
                write("import wrappers.models.LambdaProxyResponse;")

                write()

                write("public class HttpServerlessFunction$className$methodName {")

                write()

                incrementTabLevel()

                write("public void nimbusHandle(InputStream input, OutputStream output, Context context) {")

                incrementTabLevel()

                write("ObjectMapper objectMapper = new ObjectMapper();")
                write("try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {")
                incrementTabLevel()

                if (inputParam != null) {
                    write("String inputText =  buffer.lines().collect(Collectors.joining(\"\\n\"));")

                    write("String body = objectMapper.readTree(inputText).get(\"body\").asText();")

                    write("$inputParam parsedType = objectMapper.readValue(body, $inputParam.class);")
                }

                write("$className handler = new $className();")

                val callPrefix = if (returnType.toString() == "void") {
                    ""
                } else {
                    "$returnType result = "
                }

                write("LambdaProxyResponse response = new LambdaProxyResponse();")

                when {
                    inputParam == null -> write("${callPrefix}handler.$methodName(new Event(\"fuck\"));")
                    inputParamIndex == 0 -> write("${callPrefix}handler.$methodName(parsedType, new Event(\"fuck\"));")
                    else -> write("${callPrefix}handler.$methodName(new Event(\"fuck\"), parsedType);")
                }

                if (returnType.toString() != "void") {
                    write("String resultString = objectMapper.writeValueAsString(result);")
                    write("response.setBody(resultString);")
                }

                write("String responseString = objectMapper.writeValueAsString(response);")
                write("PrintWriter writer = new PrintWriter(output);")
                write("writer.print(responseString);")
                write("writer.close();")
                write("output.close();")


                decrementTabLevel()
                write("} catch (IOException e) {")
                write("\te.printStackTrace();")
                write("}")
                write("return;")

                decrementTabLevel()

                write("}")

                decrementTabLevel()

                write("}")

                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun incrementTabLevel() {
        tabLevel++
    }

    private fun decrementTabLevel() {
        tabLevel--
    }

    private fun write(toWrite: String = "") {
        var tabs = ""
        for (i in 1..tabLevel) {
            tabs += "\t"
        }
        out?.println("$tabs$toWrite")
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
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

}