package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.file.S3LambdaConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.wrappers.file.FileStorageServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.FileStorageServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.FileStorageBucketFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class FileStorageResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment,
        messager: Messager,
        private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        messager,
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
            val stages = stageService.determineStages(fileStorageFunction.stages)

            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileStorageFileBuilder.classFilePath(),
                    handlerFile = fileStorageFileBuilder.handlerFile(),
                    replacementVariable = "\${${fileStorageFileBuilder.handlerFile()}}",
                    stages = stages
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(fileStorageFunction.timeout, fileStorageFunction.memory, stage)


                val functionResource = functionEnvironmentService.newFunction(
                        fileStorageFileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                val updateResources = cfDocuments[stage]!!.updateTemplate.resources

                val bucket = resourceFinder.getFileStorageBucketResource(FileStorageBucketFunctionAnnotation(fileStorageFunction), type, stage)

                if (bucket == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find FileStorageBucket class", type)
                    return
                }

                val lambdaConfiguration = S3LambdaConfiguration(fileStorageFunction.eventType, functionResource)

                val permission = FunctionPermissionResource(functionResource, bucket, nimbusState)
                bucket.addDependsOn(functionResource)
                bucket.addDependsOn(permission)
                updateResources.addResource(permission)
                bucket.addLambdaConfiguration(lambdaConfiguration)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}