package annotation.cloudformation.resource.dynamo

import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.resource.Resource
import com.google.gson.JsonObject

class DynamoStreamResource(
        private val dynamoResource: Resource,
        nimbusState: NimbusState
): Resource(nimbusState) {
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