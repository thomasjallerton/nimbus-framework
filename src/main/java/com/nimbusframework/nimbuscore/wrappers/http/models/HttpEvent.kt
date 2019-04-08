package com.nimbusframework.nimbuscore.wrappers.http.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent

@JsonIgnoreProperties(ignoreUnknown = true)
data class HttpEvent(
        val resource: String? = null,
        val path: String? = null,
        val httpMethod: String? = null,
        val headers: Map<String, String> = mapOf(),
        val multiValueHeaders: Map<String, List<String>> = mapOf(),
        val queryStringParameters: Map<String, String> = mapOf(),
        val multiValueQueryStringParameters: Map<String, List<String>> = mapOf(),
        val pathParameters: Map<String, String> = mapOf(),
        val stageVariables: Map<String, String> = mapOf(),
        val body: String? = null,
        val isBase64Encoded: Boolean? = null
): ServerlessEvent