package cloudformation.outputs

import cloudformation.resource.http.RestApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import configuration.REST_API_URL_OUTPUT
import persisted.NimbusState

class RestApiOutput(
        private val restApi: RestApi,
        nimbusState: NimbusState): Output(nimbusState, restApi.stage) {
    override fun getName(): String {
        return REST_API_URL_OUTPUT
    }

    override fun toCloudFormation(): JsonObject {
        val restApiUrl = JsonObject()

        val joinValues = JsonArray()
        joinValues.add("https://")
        joinValues.add(restApi.getRef())
        joinValues.add(".execute-api.")
        joinValues.add(getRegion())
        joinValues.add(".amazonaws.com/$stage")


        val value = joinJson("", joinValues)

        restApiUrl.add("Value", value)

        val export = JsonObject()
        export.addProperty("Name", "${nimbusState.projectName}-$stage-$REST_API_URL_OUTPUT")

        restApiUrl.add("Export", export)

        return restApiUrl
    }
}