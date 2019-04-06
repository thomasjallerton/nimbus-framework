package annotation.services.useresources

import annotation.annotations.keyvalue.UsesKeyValueStore
import annotation.services.ResourceFinder
import annotation.wrappers.annotations.datamodel.UsesKeyValueStoreAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesKeyValueStoreHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)

        for (usesKeyValueStore in serverlessMethod.getAnnotationsByType(UsesKeyValueStore::class.java)) {
            for (stage in usesKeyValueStore.stages) {
                if (stage == functionResource.stage) {
                    val annotation = UsesKeyValueStoreAnnotation(usesKeyValueStore)
                    val resource = resourceFinder.getKeyValueStoreResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "")
                    }
                }
            }
        }
    }
}