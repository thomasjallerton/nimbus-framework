package annotation.models.resource.function

import com.google.gson.JsonObject

interface FunctionTrigger {
    fun getTriggerType(): String
    fun getArn(suffix: String): JsonObject
    fun getTriggerArn(): JsonObject
}