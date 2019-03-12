package annotation.services.functions

import annotation.annotations.function.FileStorageServerlessFunction
import annotation.annotations.function.repeatable.FileStorageServerlessFunctions
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.file.FileStorageServerlessFunctionFileBuilder
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
                type
        )

        fileStorageFileBuilder.createClass()

        for (fileStorageFunction in fileStorageFunctions) {
            for (stage in fileStorageFunction.stages) {
                val config = FunctionConfig(fileStorageFunction.timeout, fileStorageFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(fileStorageFileBuilder.getHandler(), methodInformation, config)

                functionEnvironmentService.newFileTrigger(fileStorageFunction.bucketName, fileStorageFunction.eventType, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}