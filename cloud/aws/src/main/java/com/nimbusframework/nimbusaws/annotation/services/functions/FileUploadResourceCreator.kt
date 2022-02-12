package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload
import com.nimbusframework.nimbuscore.annotations.deployment.FileUploads
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.FileUploadAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class FileUploadResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
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
    FileUpload::class.java,
    FileUploads::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val fileUploads = type.getAnnotationsByType(FileUpload::class.java)

        for (fileUpload in fileUploads) {
            val stages = stageService.determineStages(fileUpload.stages)

            for (stage in stages) {

                val bucketMap = nimbusState.fileUploads.getOrPut(stage) { mutableMapOf() }

                val bucket = resourceFinder.getFileStorageBucketResource(FileUploadAnnotation(fileUpload), type, stage)

                if (bucket == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find file storage bucket class", type)
                    return listOf()
                }

                val fileList = bucketMap.getOrPut(bucket.bucketName) { mutableListOf() }

                fileList.add(
                    FileUploadDescription(
                        fileUpload.localPath,
                        fileUpload.targetPath,
                        fileUpload.substituteNimbusVariablesFileRegex
                    )
                )
            }
        }
        return mutableListOf()
    }

}
