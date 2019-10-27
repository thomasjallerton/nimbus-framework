package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.FileStorageServerlessFunctions
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.file.FileStorageServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileStorageResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
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
            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileStorageFileBuilder.classFilePath(),
                    handlerFile = fileStorageFileBuilder.handlerFile(),
                    replacementVariable = "\${${fileStorageFileBuilder.handlerFile()}}",
                    stages = fileStorageFunction.stages.toSet()
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in fileStorageFunction.stages) {
                val config = FunctionConfig(fileStorageFunction.timeout, fileStorageFunction.memory, stage)


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