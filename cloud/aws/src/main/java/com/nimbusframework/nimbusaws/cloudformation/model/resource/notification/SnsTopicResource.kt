package com.nimbusframework.nimbusaws.cloudformation.model.resource.notification

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedNotificationTopic
import com.nimbusframework.nimbusaws.cloudformation.model.resource.DirectAccessResource
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition

class SnsTopicResource(
    val annotation: ParsedNotificationTopic,
    nimbusState: NimbusState,
    stage: String
) : Resource(nimbusState, stage), FunctionTrigger, DirectAccessResource {

    private var function: FunctionResource? = null

    fun setFunction(functionResource: FunctionResource) {
        function = functionResource
    }

    override fun getTriggerArn(): JsonObject {
        return getArn()
    }

    override fun getTriggerType(): String {
        return "sns."
    }

    override fun getTriggerName(): String {
        return "Sns"
    }

    override fun toCloudFormation(): JsonObject {
        val snsTopic = JsonObject()
        snsTopic.addProperty("Type", "AWS::SNS::Topic")

        val properties = getProperties()
        properties.addProperty("TopicName", "${annotation.topicName}$stage")
        properties.addProperty("DisplayName", "")

        if (function != null) {
            val subscription = JsonArray()
            val endpoint = JsonObject()

            endpoint.add("Endpoint", function!!.getArn())
            endpoint.addProperty("Protocol", "lambda")

            subscription.add(endpoint)

            properties.add("Subscription", subscription)
        }

        snsTopic.add("Properties", properties)


        return snsTopic
    }

    override fun getName(): String {
        return "SNSTopic${annotation.topicName}"
    }

    override fun getArn(suffix: String): JsonObject {
        return getRef()
    }
}
