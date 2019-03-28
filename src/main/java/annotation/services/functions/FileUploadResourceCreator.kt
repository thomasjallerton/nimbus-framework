package annotation.services.functions

import annotation.annotations.deployment.FileUpload
import annotation.annotations.deployment.FileUploads
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class FileUploadResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState,
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
                val fileMap = bucketMap.getOrPut("${fileUpload.bucketName}$stage".toLowerCase()) { mutableMapOf()}
                fileMap[fileUpload.localPath] = fileUpload.targetPath
            }
        }
    }

}