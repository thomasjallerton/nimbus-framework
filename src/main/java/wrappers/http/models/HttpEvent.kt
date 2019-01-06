package wrappers.http.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HttpEvent(
        val resource: String? = null,
        val path: String? = null,
        val httpMethod: String? = null,
        val headers: Map<String, String>? = null,
        val multiValueHeaders: Map<String, List<String>>? = null,
        val queryStringParameters: Map<String, String>? = null,
        val multiValueQueryStringParameters: Map<String, List<String>>? = null,
        val pathParameters: Map<String, String>? = null,
        val stageVariables: Map<String, String>? = null,
        val requestContext: RequestContext? = null,
        val body: String? = null,
        val isBase64Encoded: Boolean? = null
)