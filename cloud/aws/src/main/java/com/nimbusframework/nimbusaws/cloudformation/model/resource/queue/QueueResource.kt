package com.nimbusframework.nimbusaws.cloudformation.model.resource.queue

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.cloudformation.model.resource.DirectAccessResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition

class QueueResource(
    val definition: ParsedQueueDefinition,
    nimbusState: NimbusState,
    stage: String
) : Resource(nimbusState, stage), DirectAccessResource {

    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")

        val properties = getProperties()
        properties.addProperty("VisibilityTimeout", definition.itemProcessingTimeout)
        properties.addProperty("QueueName", "${definition.queueId}$stage")

        queue.add("Properties", properties)

        return queue
    }

    override fun getName(): String {
        return "SQSQueue${toAlphanumeric(definition.queueId)}"
    }

}
