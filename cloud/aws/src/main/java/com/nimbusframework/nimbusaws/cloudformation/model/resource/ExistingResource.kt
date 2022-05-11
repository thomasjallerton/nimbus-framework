package com.nimbusframework.nimbusaws.cloudformation.model.resource

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject

class ExistingResource(
        val arn: String,
        nimbusState: NimbusState,
        stage: String
) : Resource(nimbusState, stage) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExistingResource) return false

        if (arn != other.arn) return false

        return true
    }

    override fun hashCode(): Int {
        return arn.hashCode()
    }


}
