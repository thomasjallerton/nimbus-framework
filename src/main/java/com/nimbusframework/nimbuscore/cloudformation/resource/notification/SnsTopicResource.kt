package com.nimbusframework.nimbuscore.cloudformation.resource.notification

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class SnsTopicResource(
        private val topic: String,
        private val function: FunctionResource?,
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage), FunctionTrigger {
    override fun getTriggerArn(): JsonObject {
        return getArn()
    }

    override fun getTriggerType(): String {
        return "sns."
    }

    override fun toCloudFormation(): JsonObject {
        val snsTopic = JsonObject()
        snsTopic.addProperty("Type", "AWS::SNS::Topic")

        val properties = getProperties()
        properties.addProperty("TopicName", "$topic$stage")
        properties.addProperty("DisplayName", "")

        if (function != null) {
            val subscription = JsonArray()
            val endpoint = JsonObject()

            endpoint.add("Endpoint", function.getArn())
            endpoint.addProperty("Protocol", "lambda")

            subscription.add(endpoint)

            properties.add("Subscription", subscription)
        }

        snsTopic.add("Properties", properties)


        return snsTopic
    }

    override fun getName(): String {
        return "SNSTopic$topic"
    }

    override fun getArn(suffix: String): JsonObject {
        return getRef()
    }
}
