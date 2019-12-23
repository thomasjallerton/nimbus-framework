package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbuscore.annotations.keyvalue.UsesKeyValueStore
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesKeyValueStoreAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesKeyValueStoreProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState,
        private val messager: Messager
): UsesResourcesProcessor {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)

        for (usesKeyValueStore in serverlessMethod.getAnnotationsByType(UsesKeyValueStore::class.java)) {
            functionResource.addClient(ClientType.KeyValueStore)

            for (stage in usesKeyValueStore.stages) {
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