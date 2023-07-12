package com.nimbusframework.nimbuscore.eventabstractions

import java.util.*

data class HttpEvent(
        /**
         * Headers in the request. Keys are always lowercase.
         */
        val headers: Map<String, String>? = mapOf(),
        val queryStringParameters: Map<String, String>? = mapOf(),
        val pathParameters: Map<String, String>? = mapOf(),
        val stageVariables: Map<String, String>? = mapOf(),
        val authorizationContext: Map<String, Any>? = mapOf(),
        val body: String? = null,
        val isBase64Encoded: Boolean? = null,
        val requestId: String = UUID.randomUUID().toString()
) : ServerlessEvent
