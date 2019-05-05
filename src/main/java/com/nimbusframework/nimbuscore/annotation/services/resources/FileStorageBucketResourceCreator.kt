package com.nimbusframework.nimbuscore.annotation.services.resources

import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBucket
import com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBuckets
import com.nimbusframework.nimbuscore.annotation.wrappers.WebsiteConfiguration
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.outputs.BucketWebsiteUrlOutput
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileStorageBucketPolicy
import com.nimbusframework.nimbuscore.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
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

                val fileBucket = FileBucket(
                        nimbusState,
                        storageBucket.bucketName,
                        storageBucket.allowedCorsOrigins,
                        storageBucket.allowedCorsMethods,
                        stage
                )

                fileBucket.setWebsiteConfiguration(
                        WebsiteConfiguration(
                                storageBucket.staticWebsite,
                                storageBucket.indexFile,
                                storageBucket.errorFile
                        )
                )

                if (storageBucket.staticWebsite) {
                    cloudFormationDocuments.fileBucketWebsites.add(fileBucket)

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
