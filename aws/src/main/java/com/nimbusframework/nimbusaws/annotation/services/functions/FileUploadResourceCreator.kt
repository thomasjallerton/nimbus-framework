package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload
import com.nimbusframework.nimbuscore.annotations.deployment.FileUploads
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
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