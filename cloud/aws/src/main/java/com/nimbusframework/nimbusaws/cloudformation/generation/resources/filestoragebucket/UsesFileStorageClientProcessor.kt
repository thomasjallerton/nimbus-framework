package com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesFileStorageBucketAnnotation
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesFileStorageClientProcessor(
    private val messager: Messager,
    private val resourceFinder: ResourceFinder,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (fileStorage in serverlessMethod.getAnnotationsByType(UsesFileStorageBucket::class.java)) {

            for (stage in stageService.determineStages(fileStorage.stages)) {
                if (stage == functionResource.stage) {

                    val fileStorageBucket = resourceFinder.getFileStorageBucketResource(UsesFileStorageBucketAnnotation(fileStorage), serverlessMethod, stage)

                    if (fileStorageBucket == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find FileStorageBucket class", serverlessMethod)
                        return
                    }

                    iamRoleResource.addAllowStatement("s3:GetObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:PutObject", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:ListBucket", fileStorageBucket, "")
                    iamRoleResource.addAllowStatement("s3:GetObject", fileStorageBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:DeleteObject", fileStorageBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:PutObject", fileStorageBucket, "/*")
                    iamRoleResource.addAllowStatement("s3:ListBucket", fileStorageBucket, "/*")
                }
            }
        }
    }
}
