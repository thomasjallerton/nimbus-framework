package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.document.UsesDocumentStore
import com.nimbusframework.nimbuscore.annotation.services.ResourceFinder
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.UsesDocumentStoreAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesDocumentStoreHandler(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)
        for (usesDocumentStore in serverlessMethod.getAnnotationsByType(UsesDocumentStore::class.java)) {
            functionResource.addClient(ClientType.DocumentStore)

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