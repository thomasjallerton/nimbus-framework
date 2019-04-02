package cloudformation.outputs

import cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import persisted.NimbusState

abstract class ApiGatewayOutput(
        private val api: Resource,
        nimbusState: NimbusState,
        private val protocol: String
): Output(nimbusState, api.stage) {

    override fun getName(): String {
        return "${api.getName()}UrlOutput"
    }

    override fun toCloudFormation(): JsonObject {
        val restApiUrl = JsonObject()

        val joinValues = JsonArray()
        joinValues.add(protocol)
        joinValues.add(api.getRef())
        joinValues.add(".execute-api.")
        joinValues.add(getRegion())
        joinValues.add(".amazonaws.com/$stage")


        val value = joinJson("", joinValues)

        restApiUrl.add("Value", value)

        val export = JsonObject()
        export.addProperty("Name", getExportName())

        restApiUrl.add("Export", export)

        return restApiUrl
    }
}