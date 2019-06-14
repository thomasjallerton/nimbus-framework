package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.FileUpload
import com.nimbusframework.nimbuscore.annotation.annotations.deployment.FileUploads
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbuscore.persisted.FileUploadDescription
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileUploadResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        FileUpload::class.java,
        FileUploads::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val fileUploads = type.getAnnotationsByType(FileUpload::class.java)


        for (fileUpload in fileUploads) {
            for (stage in fileUpload.stages) {

                val bucketMap = nimbusState.fileUploads.getOrPut(stage) { mutableMapOf() }
                val fileList = bucketMap.getOrPut("${fileUpload.bucketName}$stage".toLowerCase()) { mutableListOf() }

                fileList.add(FileUploadDescription(fileUpload.localPath, fileUpload.targetPath, fileUpload.substituteNimbusVariables))
            }
        }
    }

}