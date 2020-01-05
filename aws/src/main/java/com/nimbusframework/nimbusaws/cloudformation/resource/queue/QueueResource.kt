package com.nimbusframework.nimbusaws.cloudformation.resource.queue

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource

class QueueResource(
        nimbusState: NimbusState,
        val id: String,
        private var visibilityTimeout: Int,
        stage: String
): Resource(nimbusState, stage) {

    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")

        val properties = getProperties()
        properties.addProperty("VisibilityTimeout", visibilityTimeout)
        properties.addProperty("QueueName", "$id$stage")

        queue.add("Properties", properties)

        return queue
    }

    fun updateVisibilityTimeout(functionTimeout: Int) {
        visibilityTimeout = functionTimeout * 6
    }

    override fun getName(): String {
        return "SQSQueue$id"
    }
}
