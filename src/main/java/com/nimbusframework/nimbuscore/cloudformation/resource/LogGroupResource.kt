package com.nimbusframework.nimbuscore.cloudformation.resource

import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject

class LogGroupResource(
        private val className: String,
        private val methodName: String,
        private val functionResource: FunctionResource,
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage) {

    override fun getArn(suffix: String): JsonObject {
        val arn = JsonObject()
        arn.addProperty("Fn::Sub", "arn:\${AWS::Partition}:logs:\${AWS::Region}:\${AWS::AccountId}" +
                ":log-group:/aws/lambda/${functionResource.getFunctionName()}$suffix")
        return arn
    }

    override fun getName(): String {
        return "$className${methodName}LogGroup"
    }

    override fun toCloudFormation(): JsonObject {
        val logGroupResource = JsonObject()

        logGroupResource.addProperty("Type", "AWS::Logs::LogGroup")

        val properties = getProperties()
        properties.addProperty("LogGroupName", "/aws/lambda/${functionResource.getFunctionName()}")

        logGroupResource.add("Properties", properties)

        return logGroupResource
    }

}
