package com.nimbusframework.nimbuscore.eventabstractions

import java.util.*

data class HttpEvent(
        val resource: String? = null,
        val path: String? = null,
        val httpMethod: String? = null,
        val headers: Map<String, String>? = mapOf(),
        val multiValueHeaders: Map<String, List<String>>? = mapOf(),
        val queryStringParameters: Map<String, String>? = mapOf(),
        val multiValueQueryStringParameters: Map<String, List<String>>? = mapOf(),
        val pathParameters: Map<String, String>? = mapOf(),
        val stageVariables: Map<String, String>? = mapOf(),
        val authorizationContext: Map<String, Any>? = mapOf(),
        val body: String? = null,
        val isBase64Encoded: Boolean? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent
