package annotation.services.resources

import annotation.annotations.file.FileStorageBucket
import annotation.annotations.file.FileStorageBuckets
import annotation.wrappers.WebsiteConfiguration
import cloudformation.CloudFormationDocuments
import cloudformation.resource.file.FileBucket
import cloudformation.resource.file.FileStorageBucketPolicy
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
                }

                updateResources.addResource(fileBucket)
            }
        }
    }

}
