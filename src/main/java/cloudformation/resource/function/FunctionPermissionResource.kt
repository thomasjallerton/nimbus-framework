package cloudformation.resource.function

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class FunctionPermissionResource(
        private val function: FunctionResource,
        private val trigger: FunctionTrigger,
        nimbusState: NimbusState
): Resource(nimbusState) {
    override fun toCloudFormation(): JsonObject {
        val permission = JsonObject()
        permission.addProperty("Type", "AWS::Lambda::Permission")

        val properties = getProperties()
        properties.add("FunctionName", function.getArn())
        properties.addProperty("Action", "lambda:InvokeFunction")

        val principal = JsonObject()

        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add(trigger.getTriggerType())

        val urlSuffixRef = JsonObject()
        urlSuffixRef.addProperty("Ref", "AWS::URLSuffix")
        join.add(urlSuffixRef)

        joinFunc.add(join)

        principal.add("Fn::Join", joinFunc)

        properties.add("Principal", principal)

                properties.add("SourceArn", trigger.getTriggerArn())

        permission.add("Properties", properties)

        return permission
    }

    override fun getName(): String {
        return function.getName() + "PermissionApiGateway"
    }
}