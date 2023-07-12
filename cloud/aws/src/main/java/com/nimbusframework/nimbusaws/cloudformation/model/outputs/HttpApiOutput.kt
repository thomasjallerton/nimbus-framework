package com.nimbusframework.nimbusaws.cloudformation.model.outputs

import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.HttpApi
import com.nimbusframework.nimbusaws.configuration.REST_API_URL_OUTPUT
import com.nimbusframework.nimbuscore.persisted.NimbusState

class HttpApiOutput(
    restApi: HttpApi,
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
