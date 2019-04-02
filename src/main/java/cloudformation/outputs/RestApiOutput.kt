package cloudformation.outputs

import cloudformation.resource.http.RestApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import configuration.REST_API_URL_OUTPUT
import configuration.WEBSOCKET_API_URL_OUTPUT
import persisted.NimbusState

class RestApiOutput(
        restApi: RestApi,
        nimbusState: NimbusState
): ApiGatewayOutput(
        restApi,
        nimbusState,
        "https://"
) {
    override fun getExportName(): String {
        return "${nimbusState.projectName}-$stage-$REST_API_URL_OUTPUT"
    }
}