package com.nimbusframework.nimbuslocal.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.RequestContext
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent

class WebSocketEventBuilder {

    private var headers: MutableMap<String, String> = mutableMapOf()
    private var multiValueHeaders: MutableMap<String, List<String>> = mutableMapOf()
    private var queryStringParameters: MutableMap<String, String> = mutableMapOf()
    private var multiValueQueryStringParameters: MutableMap<String, List<String>> = mutableMapOf()
    private var body: String? = null
    private var requestContext: RequestContext = RequestContext()
    private var isBase64Encoded: Boolean? = null
    private var requestId: String = ""

    fun withHeaders(headers: MutableMap<String, String>): WebSocketEventBuilder {
        this.headers = headers
        return this
    }

    fun withHeader(header: String, value: String): WebSocketEventBuilder {
        this.headers[header] = value
        return this
    }

    fun withMultiValueHeaders(multiValueHeaders: MutableMap<String, List<String>>): WebSocketEventBuilder {
        this.multiValueHeaders = multiValueHeaders
        return this
    }

    fun withMultiValueHeader(multiValueHeader: String, value: List<String>): WebSocketEventBuilder {
        this.multiValueHeaders[multiValueHeader] = value
        return this
    }

    fun withQueryStringParameters(queryStringParameters: MutableMap<String, String>): WebSocketEventBuilder {
        this.queryStringParameters = queryStringParameters
        return this
    }

    fun withQueryStringParameter(queryStringParameter: String, value: String): WebSocketEventBuilder {
        this.queryStringParameters[queryStringParameter] = value
        return this
    }

    fun withMultiValueQueryStringParameters(multiValueQueryStringParameters: MutableMap<String, List<String>>): WebSocketEventBuilder {
        this.multiValueQueryStringParameters = multiValueQueryStringParameters
        return this
    }

    fun withMultiValueQueryStringParameter(multiValueQueryStringParameter: String, value: List<String>): WebSocketEventBuilder {
        this.multiValueQueryStringParameters[multiValueQueryStringParameter] = value
        return this
    }

    fun withBody(body: String): WebSocketEventBuilder {
        this.body = body
        return this
    }

    fun withRequestContext(requestContext: RequestContext): WebSocketEventBuilder {
        this.requestContext = requestContext
        return this
    }

    fun withIsBase64Encoded(isBase64Encoded: Boolean): WebSocketEventBuilder {
        this.isBase64Encoded = isBase64Encoded
        return this
    }

    fun withRequestId(requestId: String): WebSocketEventBuilder {
        this.requestId = requestId
        return this
    }

    fun build(): WebSocketEvent {
        return WebSocketEvent(
            headers,
            multiValueHeaders,
            queryStringParameters,
            multiValueQueryStringParameters,
            body,
            requestContext,
            isBase64Encoded,
            requestId
        )
    }

}