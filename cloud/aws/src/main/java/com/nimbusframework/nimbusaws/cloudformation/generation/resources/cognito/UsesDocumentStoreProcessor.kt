package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.document.UsesDocumentStore
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesDocumentStoreAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesDocumentStoreProcessor(
    private val cfDocuments: Map<String, CloudFormationFiles>,
    private val processingEnv: ProcessingEnvironment,
    nimbusState: NimbusState,
    private val messager: Messager
): UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.iamRoleResource
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)
        for (usesDocumentStore in serverlessMethod.getAnnotationsByType(UsesDocumentStore::class.java)) {

            for (stage in stageService.determineStages(usesDocumentStore.stages)) {
                if (stage == functionResource.stage) {
                    val dataModelAnnotation = UsesDocumentStoreAnnotation(usesDocumentStore)
                    val resource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, serverlessMethod, stage)

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "")
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Could not find document store associated with method parameter of UsesDocumentStore", serverlessMethod)
                    }
                }
            }
        }
    }
}
