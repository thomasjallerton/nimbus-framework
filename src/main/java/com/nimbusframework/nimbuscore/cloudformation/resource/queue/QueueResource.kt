package com.nimbusframework.nimbuscore.cloudformation.resource.queue

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject

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
        return "NimbusSQSQueue$name"
    }
}
