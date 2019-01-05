package annotation.models.resource

import com.google.gson.JsonObject

interface FunctionTrigger {
    fun getTriggerType(): String
    fun getArn(suffix: String): JsonObject
    fun getTriggerArn(): JsonObject
}