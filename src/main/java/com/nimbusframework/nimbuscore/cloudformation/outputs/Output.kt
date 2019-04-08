package com.nimbusframework.nimbuscore.cloudformation.outputs

import com.google.gson.JsonArray
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject

abstract class Output(protected val nimbusState: NimbusState, protected val stage: String) {
    abstract fun getName(): String
    abstract fun toCloudFormation(): JsonObject

    fun joinJson(delimiter: String, values: JsonArray): JsonObject {
        val join = JsonObject()
        val params = JsonArray()
        params.add(delimiter)
        params.add(values)

        join.add("Fn::Join", params)

        return join
    }

    fun getRegion(): JsonObject {
        val region = JsonObject()
        region.addProperty("Ref", "AWS::Region")
        return region
    }

    abstract fun getExportName(): String
}