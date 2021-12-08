package com.nimbusframework.nimbusaws.cloudformation.resource.function

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class FunctionPermissionResource(
        private val function: FunctionResource,
        private val trigger: FunctionTrigger,
        nimbusState: NimbusState
): Resource(nimbusState, function.stage) {
    override fun toCloudFormation(): JsonObject {
        val permission = JsonObject()
        permission.addProperty("Type", "AWS::Lambda::Permission")

        val properties = getProperties()
        properties.add("FunctionName", function.getArn())
        properties.addProperty("Action", "lambda:InvokeFunction")

        val joinValues = JsonArray()
        joinValues.add(trigger.getTriggerType())

        val urlSuffixRef = JsonObject()
        urlSuffixRef.addProperty("Ref", "AWS::URLSuffix")
        joinValues.add(urlSuffixRef)


        val principal = joinJson("", joinValues)

        properties.add("Principal", principal)

        if (trigger.getTriggerArn() != JsonObject()) {
            properties.add("SourceArn", trigger.getTriggerArn())
        }

        permission.add("Properties", properties)

        return permission
    }

    override fun getName(): String {
        return "LambdaPerm${trigger.getTriggerName()}" + function.getShortName()
    }
}