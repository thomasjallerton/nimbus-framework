package com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.keyvalue.UsesKeyValueStore
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesKeyValueStoreAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesKeyValueStoreProcessor(
    private val cfDocuments: Map<String, CloudFormationFiles>,
    private val processingEnv: ProcessingEnvironment,
    nimbusState: NimbusState,
    private val messager: Messager
): UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.iamRoleResource
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)

        for (usesKeyValueStore in serverlessMethod.getAnnotationsByType(UsesKeyValueStore::class.java)) {

            for (stage in stageService.determineStages(usesKeyValueStore.stages)) {
                if (stage == functionResource.stage) {
                    val annotation = UsesKeyValueStoreAnnotation(usesKeyValueStore)
                    val resource = resourceFinder.getKeyValueStoreResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "")
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Could not find key-value store associated with method parameter of UsesKeyValueStore", serverlessMethod)
                    }
                }
            }
        }
    }
}
