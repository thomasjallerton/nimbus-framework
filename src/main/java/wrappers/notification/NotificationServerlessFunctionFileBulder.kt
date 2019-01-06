package wrappers.notification

import annotation.models.processing.MethodInformation
import wrappers.ServerlessFunctionFileBuilder
import wrappers.notification.models.NotificationEvent
import wrappers.notification.models.RecordCollection
import wrappers.notification.models.SnsMessageFormat
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

class NotificationServerlessFunctionFileBulder(
        private val processingEnv: ProcessingEnvironment,
        private val methodInformation: MethodInformation
): ServerlessFunctionFileBuilder(processingEnv, methodInformation) {


    override fun getGeneratedClassName(): String {
        return "NotificationServerlessFunction${methodInformation.className}${methodInformation.methodName}"
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

                write("public class ${getGeneratedClassName()} {")

                write()

                write("public void nimbusHandle(InputStream input, OutputStream output, Context context) {")

                write("ObjectMapper objectMapper = new ObjectMapper();")
                write("try {")

                write("String jsonString = new BufferedReader(new InputStreamReader(input)).lines().collect(Collectors.joining(\"\\n\"));")

                writeInputs(inputParam)

                write("${methodInformation.className} handler = new ${methodInformation.className}();")

                writeFunction(inputParam)

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

    private fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${NotificationEvent::class.qualifiedName};")
        write("import ${RecordCollection::class.qualifiedName};")
        write("import ${SnsMessageFormat::class.qualifiedName};")

        write()
    }

    private fun writeInputs(inputParam: InputParam) {

        write("RecordCollection records = objectMapper.readValue(jsonString, RecordCollection.class);")

        if (inputParam.type != null) {
            write("NotificationEvent event = records.getRecords().get(0).getSns();")
            write("SnsMessageFormat snsFormat = objectMapper.readValue(event.getMessage(), SnsMessageFormat.class);")
            write("${inputParam.type} parsedType;")
            write("if (snsFormat.getLambda() != null) {")
            write("parsedType = objectMapper.readValue(snsFormat.getLambda(), ${inputParam.type}.class);")
            write("} else if (snsFormat.getDefault() != null) {")
            write("parsedType = objectMapper.readValue(snsFormat.getDefault(), ${inputParam.type}.class);")
            write("} else {")
            write("return;")
            write("}")
        }

    }

    private fun writeFunction(inputParam: InputParam) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        when {
            inputParam.index == 0 -> write("handler.${methodInformation.methodName}(parsedType, event);")
            else -> write("handler.${methodInformation.methodName}(event, parsedType);")
        }
    }

    private fun writeHandleError() {
        write("e.printStackTrace();")
    }
}