package com.nimbusframework.nimbusaws.wrappers.file

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileStorageServerlessFunctionFileBuilder(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    classForReflectionService: ClassForReflectionService
): ServerlessFunctionFileBuilder(
        processingEnv,
        fileBuilderMethodInformation,
        FileStorageServerlessFunction::class.java.simpleName,
        FileStorageEvent::class.java,
        compilingElement,
        S3Event::class.java,
        Void::class.java,
        classForReflectionService
) {

    override fun generateClassName(): String {
        return "FileStorageServerlessFunction${fileBuilderMethodInformation.className}${fileBuilderMethodInformation.methodName}"
    }

    override fun writeImports() {
        write("import ${S3EventNotification::class.qualifiedName};")
        write("import ${S3EventMapper::class.qualifiedName};")
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        write("${S3EventNotification.S3ObjectEntity::class.java.canonicalName} objectEntity = input.getRecords().get(0).getS3().getObject();")

        if (!eventParam.doesNotExist()) {
            write("$eventSimpleName event = ${S3EventMapper::class.java.simpleName}.getFileStorageEvent(objectEntity, requestId);")
        }

        val methodName = fileBuilderMethodInformation.methodName
        when {
            eventParam.doesNotExist() -> write("handler.$methodName();")
            else -> write("handler.$methodName(event);")
        }
        write("return null;")
    }

    override fun isValidFunction(functionParams: FunctionParams) {
        if (functionParams.inputParam.index != -1) {
            compilationError("FileStorageServerlessFunction cannot have a custom user type, only a maximum of one FileStorageEvent type")
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }
}
