package com.nimbusframework.nimbusaws.cloudformation.outputs

import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

abstract class ApiGatewayOutput(
        private val api: Resource,
        nimbusState: NimbusState,
        private val protocol: String
): Output(nimbusState, api.stage) {

    override fun getName(): String {
        return "${api.getName()}UrlOutput"
    }

    fun getValue(): JsonObject {
        val joinValues = JsonArray()
        joinValues.add(protocol)
        joinValues.add(api.getRef())
        joinValues.add(".execute-api.")
        joinValues.add(getRegion())
        joinValues.add(".amazonaws.com/$stage")


        return joinJson("", joinValues)
    }


    override fun toCloudFormation(): JsonObject {
        val restApiUrl = JsonObject()

        val value = getValue()

        restApiUrl.add("Value", value)

        val export = JsonObject()
        export.addProperty("Name", getExportName())

        restApiUrl.add("Export", export)

        return restApiUrl
    }
}