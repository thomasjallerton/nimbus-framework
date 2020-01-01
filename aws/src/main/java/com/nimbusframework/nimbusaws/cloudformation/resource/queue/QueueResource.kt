package com.nimbusframework.nimbusaws.cloudformation.resource.queue

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource

class QueueResource(
        nimbusState: NimbusState,
        private val name: String,
        private val visibilityTimeout: Int,
        stage: String
): Resource(nimbusState, stage) {
    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")

        val properties = getProperties()
        properties.addProperty("VisibilityTimeout", visibilityTimeout)
        properties.addProperty("QueueName", "$name$stage")

        queue.add("Properties", properties)

        return queue
    }

    override fun getName(): String {
        return "SQSQueue$name"
    }
}
