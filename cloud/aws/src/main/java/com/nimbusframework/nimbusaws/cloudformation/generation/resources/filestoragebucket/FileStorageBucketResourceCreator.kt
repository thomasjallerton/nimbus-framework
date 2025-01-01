package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.BucketWebsiteUrlOutput
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileStorageBucketPolicy
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinitions
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.WebsiteConfiguration
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class FileStorageBucketResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        nimbusState,
        FileStorageBucketDefinition::class.java,
        FileStorageBucketDefinitions::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val storageBuckets = type.getAnnotationsByType(FileStorageBucketDefinition::class.java)

        for (storageBucket in storageBuckets) {
            for (stage in stageService.determineStages(storageBucket.stages)) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateTemplate = cloudFormationDocuments.updateTemplate
                val updateResources = updateTemplate.resources

                val fileBucket = FileBucketResource(
                        nimbusState,
                        storageBucket.bucketName,
                        storageBucket.allowedCorsOrigins,
                        stage
                )

                fileBucket.setWebsiteConfiguration(
                        WebsiteConfiguration(
                                storageBucket.staticWebsite,
                                storageBucket.websitePublicAccess,
                                storageBucket.indexFile,
                                storageBucket.errorFile
                        )
                )

                if (storageBucket.staticWebsite) {
                    updateTemplate.fileBucketWebsites.add(fileBucket)

                    val fileBucketPolicy = FileStorageBucketPolicy(nimbusState, fileBucket, stage)
                    updateResources.addResource(fileBucketPolicy)
                    val updateOutputs = updateTemplate.outputs

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

                updateResources.addFileStorageBucket(fileBucket)
            }
        }
    }

}
