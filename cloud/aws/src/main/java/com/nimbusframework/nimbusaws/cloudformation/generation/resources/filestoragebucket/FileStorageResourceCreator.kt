package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.S3LambdaConfiguration
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.wrappers.file.FileStorageServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.FileStorageServerlessFunctions
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.FileStorageBucketFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class FileStorageResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager,
    private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
    messager,
    FileStorageServerlessFunction::class.java,
    FileStorageServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val fileStorageFunctions = type.getAnnotationsByType(FileStorageServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)
        val fileStorageFileBuilder = FileStorageServerlessFunctionFileBuilder(
            processingEnv,
            methodInformation,
            type,
            classForReflectionService
        )

        fileStorageFileBuilder.createClass()

        for (fileStorageFunction in fileStorageFunctions) {
            val stages = stageService.determineStages(fileStorageFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileStorageFileBuilder)
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(fileStorageFunction.timeout, fileStorageFunction.memory, stage)


                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                val updateResources = cfDocuments[stage]!!.updateTemplate.resources

                val bucket = resourceFinder.getFileStorageBucketResource(
                    FileStorageBucketFunctionAnnotation(fileStorageFunction),
                    type,
                    stage
                )

                if (bucket == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find FileStorageBucket class", type)
                    return listOf()
                }

                val lambdaConfiguration = S3LambdaConfiguration(fileStorageFunction.eventType, functionResource)

                val permission = FunctionPermissionResource(functionResource, bucket, nimbusState)
                bucket.addDependsOn(functionResource)
                bucket.addDependsOn(permission)
                updateResources.addResource(permission)
                bucket.addLambdaConfiguration(lambdaConfiguration)

                results.add(FunctionInformation(type, functionResource, fileStorageFileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }
}
