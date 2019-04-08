package com.nimbusframework.nimbuscore.wrappers.websocket.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebSocketEvent(
        val headers: Map<String, String> = mapOf(),
        val multiValueHeaders: Map<String, List<String>> = mapOf(),
        val queryStringParameters: Map<String, String> = mapOf(),
        val multiValueQueryStringParameters: Map<String, List<String>> = mapOf(),
        val body: String? = null,
        val requestContext: RequestContext = RequestContext(),
        val isBase64Encoded: Boolean? = null
): ServerlessEvent