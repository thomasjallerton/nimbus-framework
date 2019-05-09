package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.FileStorageServerlessFunctions
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.file.FileStorageServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileStorageResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        FileStorageServerlessFunction::class.java,
        FileStorageServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val fileStorageFunctions = type.getAnnotationsByType(FileStorageServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)
        val fileStorageFileBuilder = FileStorageServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        fileStorageFileBuilder.createClass()

        for (fileStorageFunction in fileStorageFunctions) {
            for (stage in fileStorageFunction.stages) {
                val config = FunctionConfig(fileStorageFunction.timeout, fileStorageFunction.memory, stage)
                val handlerInformation = HandlerInformation(
                        handlerClassPath = fileStorageFileBuilder.classFilePath(),
                        handlerFile = fileStorageFileBuilder.handlerFile(),
                        replacementVariable = "\${${fileStorageFileBuilder.handlerFile()}}"
                )

                val functionResource = functionEnvironmentService.newFunction(
                        fileStorageFileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                functionEnvironmentService.newFileTrigger(fileStorageFunction.bucketName, fileStorageFunction.eventType, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}