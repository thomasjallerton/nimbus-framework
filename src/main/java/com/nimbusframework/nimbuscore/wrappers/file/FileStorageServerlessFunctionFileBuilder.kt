package com.nimbusframework.nimbuscore.wrappers.file

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.event.S3EventNotification
import com.nimbusframework.nimbuscore.annotation.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.wrappers.file.models.FileStorageEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileStorageServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        nimbusState: NimbusState
): ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        FileStorageServerlessFunction::class.java.simpleName,
        FileStorageEvent::class.java,
        compilingElement,
        S3Event::class.java,
        Void::class.java,
        nimbusState
) {

    override fun getGeneratedClassName(): String {
        return "FileStorageServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun writeImports() {
        write("import ${S3EventNotification::class.qualifiedName};")
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("${S3EventNotification.S3ObjectEntity::class.java.canonicalName} objectEntity = input.getRecords().get(0).getS3().getObject();")

        if (!eventParam.doesNotExist()) {
            write("$eventSimpleName event = new $eventSimpleName(objectEntity, requestId);")
        }

        val methodName = methodInformation.methodName
        when {
            eventParam.doesNotExist() -> write("handler.$methodName();")
            else -> write("handler.$methodName(event);")
        }
        write("return null;")
    }

    override fun isValidFunction(functionParams: ServerlessFunctionFileBuilder.FunctionParams) {
        if (functionParams.inputParam.index != -1) {
            compilationError("FileStorageServerlessFunction cannot have a custom user type, only a maximum of one FileStorageEvent type")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }
}