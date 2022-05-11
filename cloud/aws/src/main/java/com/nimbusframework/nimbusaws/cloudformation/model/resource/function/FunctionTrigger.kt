package com.nimbusframework.nimbusaws.cloudformation.model.resource.function

import com.google.gson.JsonObject

interface FunctionTrigger {
    fun getTriggerType(): String
    fun getTriggerName(): String
    fun getArn(suffix: String): JsonObject
    fun getTriggerArn(): JsonObject
}
