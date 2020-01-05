package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesQueueFunctionAnnotation
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesQueueProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val messager: Messager,
        private val resourceFinder: ResourceFinder
): UsesResourcesProcessor  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (usesQueue in serverlessMethod.getAnnotationsByType(UsesQueue::class.java)) {
            functionResource.addClient(ClientType.Queue)

            for (stage in usesQueue.stages) {
                if (stage == functionResource.stage) {

                    val cloudFormationDocuments = cfDocuments[stage]

                    val queue = resourceFinder.getQueueResource(UsesQueueFunctionAnnotation(usesQueue), serverlessMethod, stage)

                    if (queue == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find queue class", serverlessMethod)
                        return
                    }

                    val referencedQueue = cloudFormationDocuments?.updateTemplate?.resources?.get(queue.getName())
                    if (cloudFormationDocuments == null || referencedQueue == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find id of queue, have you set it in the @QueueServerlessFunction?", serverlessMethod)
                        return
                    }

                    functionResource.addEnvVariable("NIMBUS_QUEUE_URL_ID_" + queue.id.toUpperCase(), referencedQueue.getRef())
                    iamRoleResource.addAllowStatement("sqs:SendMessage", referencedQueue, "")
                }
            }
        }
    }
}