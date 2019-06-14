package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.file.UsesFileStorage
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

class UsesFileStorageClientHandler(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val nimbusState: NimbusState
): UsesResourcesHandler  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (fileStorage in serverlessMethod.getAnnotationsByType(UsesFileStorage::class.java)) {
            functionResource.addClient(ClientType.FileStorage)

            for (stage in fileStorage.stages) {
                if (stage == functionResource.stage) {

                    val newBucket = FileBucket(nimbusState, fileStorage.bucketName, arrayOf(), stage)

                    val updateResources = cfDocuments.getValue(stage).updateTemplate.resources
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