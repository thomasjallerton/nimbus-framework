package com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesQueueFunctionAnnotation
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesQueueProcessor(
    private val messager: Messager,
    private val resourceFinder: ResourceFinder,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.iamRoleResource

        for (usesQueue in serverlessMethod.getAnnotationsByType(UsesQueue::class.java)) {
            for (stage in stageService.determineStages(usesQueue.stages)) {
                if (stage == functionResource.stage) {

                    val queue = resourceFinder.getQueueResource(UsesQueueFunctionAnnotation(usesQueue), serverlessMethod, stage)

                    if (queue == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find queue class", serverlessMethod)
                        return
                    }

                    functionResource.addEnvVariable(QueueUrlEnvironmentVariable(queue.definition), queue.getRef())
                    iamRoleResource.addAllowStatement("sqs:SendMessage", queue, "")
                }
            }
        }
    }
}
