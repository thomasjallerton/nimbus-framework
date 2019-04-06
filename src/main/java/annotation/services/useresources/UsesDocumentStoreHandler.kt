package annotation.services.useresources

import annotation.annotations.document.UsesDocumentStore
import annotation.services.ResourceFinder
import annotation.wrappers.annotations.datamodel.UsesDocumentStoreAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesDocumentStoreHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)
        for (usesDocumentStore in serverlessMethod.getAnnotationsByType(UsesDocumentStore::class.java)) {
            for (stage in usesDocumentStore.stages) {
                if (stage == functionResource.stage) {
                    val dataModelAnnotation = UsesDocumentStoreAnnotation(usesDocumentStore)
                    val resource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, serverlessMethod, stage)

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "")
                    }
                }
            }
        }
    }
}