package annotation.services.useresources

import annotation.annotations.file.UsesFileStorageClient
import cloudformation.CloudFormationDocuments
import cloudformation.resource.file.FileBucket
import cloudformation.resource.function.FunctionResource
import persisted.NimbusState
import javax.lang.model.element.Element

class UsesFileStorageClientHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState
): UsesResourcesHandler  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (fileStorage in serverlessMethod.getAnnotationsByType(UsesFileStorageClient::class.java)) {
            for (stage in fileStorage.stages) {
                if (stage == functionResource.stage) {

                    val newBucket = FileBucket(nimbusState, fileStorage.bucketName, stage)

                    val updateResources = cfDocuments.getValue(stage).updateResources
                    val existingBucket = updateResources.get(newBucket.getName()) as FileBucket?

                    var permissionsBucket = existingBucket
                    if (existingBucket == null) {
                        updateResources.addResource(newBucket)
                        permissionsBucket = newBucket
                    }
                    functionResource.addEnvVariable("NIMBUS_STAGE", stage)
                    iamRoleResource.addAllowStatement("s3:GetObject", permissionsBucket!!, "")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", permissionsBucket, "")
                    iamRoleResource.addAllowStatement("s3:PutObject", permissionsBucket, "")
                    iamRoleResource.addAllowStatement("s3:GetObject", permissionsBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", permissionsBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:PutObject", permissionsBucket, "/*")
                }
            }
        }    }
}