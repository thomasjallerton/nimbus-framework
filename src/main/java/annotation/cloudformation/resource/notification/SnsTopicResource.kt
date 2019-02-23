package annotation.cloudformation.resource.notification

import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.resource.Resource
import annotation.cloudformation.resource.function.FunctionResource
import annotation.cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class SnsTopicResource(
        private val topic: String,
        private val function: FunctionResource,
        nimbusState: NimbusState
): Resource(nimbusState), FunctionTrigger {
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
        properties.addProperty("TopicName", topic)
        properties.addProperty("DisplayName", "")

        val subscription = JsonArray()
        val endpoint = JsonObject()

        endpoint.add("Endpoint", function.getArn())
        endpoint.addProperty("Protocol", "lambda")

        subscription.add(endpoint)

        properties.add("Subscription", subscription)

        snsTopic.add("Properties", properties)


        return snsTopic
    }

    override fun getName(): String {
        return "SNSTopic$topic"
    }

    override fun getArn(suffix: String): JsonObject {
        val arn = JsonObject()
        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add("arn:")

        join.add(getRefProperty("AWS::Partition"))

        join.add(":sns:")

        join.add(getRefProperty("AWS::Region"))

        join.add(":")

        join.add(getRefProperty("AWS::AccountId"))

        join.add(":$topic$suffix")

        joinFunc.add(join)

        arn.add("Fn::Join", joinFunc)

        return arn
    }
}
