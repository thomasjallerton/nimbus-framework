package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.queue.UsesQueue
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.cloudformation.resource.queue.QueueResource
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesQueueHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        processingEnv: ProcessingEnvironment
): UsesResourcesHandler  {

    private val messager = processingEnv.messager

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (usesQueue in serverlessMethod.getAnnotationsByType(UsesQueue::class.java)) {
            for (stage in usesQueue.stages) {
                if (stage == functionResource.stage) {

                    val cloudFormationDocuments = cfDocuments[stage]

                    if (cloudFormationDocuments == null || usesQueue.id == "" || cloudFormationDocuments.savedResources[usesQueue.id] == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find id of queue, have you set it in the @QueueServerlessFunction?", serverlessMethod)
                        return
                    }

                    val referencedQueue = cloudFormationDocuments.savedResources[usesQueue.id]
                    if (referencedQueue !is QueueResource) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Resource with id " + usesQueue.id + " is not a Queue", serverlessMethod)
                        return
                    }

                    functionResource.addEnvVariable("NIMBUS_QUEUE_URL_ID_" + usesQueue.id.toUpperCase(), referencedQueue.getRef())
                    iamRoleResource.addAllowStatement("sqs:SendMessage", referencedQueue, "")
                }
            }
        }
    }
}