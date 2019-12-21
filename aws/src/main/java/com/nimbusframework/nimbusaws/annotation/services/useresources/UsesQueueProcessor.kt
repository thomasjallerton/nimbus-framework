package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesQueueProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesProcessor  {

    private val messager = processingEnv.messager

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (usesQueue in serverlessMethod.getAnnotationsByType(UsesQueue::class.java)) {
            functionResource.addClient(ClientType.Queue)

            for (stage in usesQueue.stages) {
                if (stage == functionResource.stage) {

                    val cloudFormationDocuments = cfDocuments[stage]

                    val queue = QueueResource(nimbusState, usesQueue.id, 10, stage)
                    val referencedQueue = cloudFormationDocuments?.updateTemplate?.resources?.get(queue.getName())
                    if (cloudFormationDocuments == null || referencedQueue == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find id of queue, have you set it in the @QueueServerlessFunction?", serverlessMethod)
                        return
                    }

                    functionResource.addEnvVariable("NIMBUS_QUEUE_URL_ID_" + usesQueue.id.toUpperCase(), referencedQueue.getRef())
                    iamRoleResource.addAllowStatement("sqs:SendMessage", referencedQueue, "")
                }
            }
        }
    }
}