package com.nimbusframework.nimbuscore.cloudformation.resource.dynamo

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject

class DynamoStreamResource(
        private val dynamoResource: Resource,
        nimbusState: NimbusState
): Resource(nimbusState, dynamoResource.stage) {
    override fun toCloudFormation(): JsonObject {
        return JsonObject()
    }

    override fun getName(): String {
        return ""
    }

    override fun getArn(suffix: String): JsonObject {
        return dynamoResource.getAttribute("StreamArn")
    }
}