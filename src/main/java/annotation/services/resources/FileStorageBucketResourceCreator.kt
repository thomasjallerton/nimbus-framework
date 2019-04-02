package annotation.services.resources

import annotation.annotations.file.FileStorageBucket
import annotation.annotations.file.FileStorageBuckets
import annotation.wrappers.WebsiteConfiguration
import cloudformation.CloudFormationDocuments
import cloudformation.outputs.BucketWebsiteUrlOutput
import cloudformation.resource.file.FileBucket
import cloudformation.resource.file.FileStorageBucketPolicy
import persisted.ExportInformation
import persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class FileStorageBucketResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        FileStorageBucket::class.java,
        FileStorageBuckets::class.java
) {

    override fun handleType(type: Element) {
        val storageBuckets = type.getAnnotationsByType(FileStorageBucket::class.java)

        for (storageBucket in storageBuckets) {
            for (stage in storageBucket.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }

                val updateResources = cloudFormationDocuments.updateResources

                val fileBucket = FileBucket(nimbusState, storageBucket.bucketName, stage)

                fileBucket.setWebsiteConfiguration(
                        WebsiteConfiguration(
                                storageBucket.staticWebsite,
                                storageBucket.indexFile,
                                storageBucket.errorFile
                        )
                )

                if (storageBucket.staticWebsite) {
                    val fileBucketPolicy = FileStorageBucketPolicy(nimbusState, fileBucket, stage)
                    updateResources.addResource(fileBucketPolicy)

                    val updateOutputs = cloudFormationDocuments.updateOutputs

                    val websiteOutputUrl = BucketWebsiteUrlOutput(fileBucket, nimbusState)
                    updateOutputs.addOutput(websiteOutputUrl)

                    val stageExports = nimbusState.exports.getOrPut(stage) { mutableListOf()}

                    val exportInformation = ExportInformation(
                            websiteOutputUrl.getExportName(),
                            "Created File Storage bucket as a static website. Base URL is ",
                            "\${${storageBucket.bucketName.toUpperCase()}_URL}"
                    )

                    stageExports.add(exportInformation)
                }

                updateResources.addResource(fileBucket)
            }
        }
    }

}
