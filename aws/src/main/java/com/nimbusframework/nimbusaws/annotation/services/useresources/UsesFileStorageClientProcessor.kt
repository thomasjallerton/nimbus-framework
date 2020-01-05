package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesFileStorageBucketAnnotation
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesFileStorageClientProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val messager: Messager,
        private val resourceFinder: ResourceFinder
): UsesResourcesProcessor  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (fileStorage in serverlessMethod.getAnnotationsByType(UsesFileStorageBucket::class.java)) {
            functionResource.addClient(ClientType.FileStorage)

            for (stage in fileStorage.stages) {
                if (stage == functionResource.stage) {

                    val fileStorageBucket = resourceFinder.getFileStorageBucketResource(UsesFileStorageBucketAnnotation(fileStorage), serverlessMethod, stage)

                    if (fileStorageBucket == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find FileStorageBucket class", serverlessMethod)
                        return
                    }

                    iamRoleResource.addAllowStatement("s3:GetObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:PutObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:GetObject", fileStorageBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", fileStorageBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:PutObject", fileStorageBucket, "/*")
                }
            }
        }    }
}