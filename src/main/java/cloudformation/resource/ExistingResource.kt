package cloudformation.resource

import persisted.NimbusState
import com.google.gson.JsonObject

class ExistingResource(
        private val arn: String,
        nimbusState: NimbusState
) : Resource(nimbusState) {
    override fun toCloudFormation(): JsonObject {
        return JsonObject()
    }

    override fun getName(): String {
        return ""
    }

    override fun getArn(suffix: String): JsonObject {
        val arnJson = JsonObject()
        arnJson.addProperty("Arn", arn)
        return arnJson
    }
}
