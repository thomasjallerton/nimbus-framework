package com.nimbusframework.nimbuslocal.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import java.util.*

class HttpEventBuilder {

    private var resource: String = ""
    private var path: String = ""
    private var httpMethod: String = "GET"
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var queryStringParameters: MutableMap<String, String> = mutableMapOf()
    private var pathParameters: MutableMap<String, String> = mutableMapOf()
    private var stageVariables: MutableMap<String, String> = mutableMapOf()
    private var authorizationContext: MutableMap<String, Any> = mutableMapOf()
    private var body: String? = null
    private var isBase64Encoded: Boolean = false
    private var requestId: String = UUID.randomUUID().toString()

    fun withResource(resource: String): HttpEventBuilder {
        this.resource = resource
        return this
    }

    fun withPath(path: String): HttpEventBuilder {
        this.path = path
        return this
    }

    fun withHttpMethod(httpMethod: String): HttpEventBuilder {
        this.httpMethod
        return this
    }

    fun withHeaders(headers: MutableMap<String, String>): HttpEventBuilder {
        this.headers = headers
        return this
    }

    fun withHeader(header: String, value: String): HttpEventBuilder {
        this.headers[header] = value
        return this
    }

    fun withQueryStringParameters(queryStringParameters: MutableMap<String, String>): HttpEventBuilder {
        this.queryStringParameters = queryStringParameters
        return this
    }

    fun withQueryStringParameter(queryStringParameter: String, value: String): HttpEventBuilder {
        this.queryStringParameters[queryStringParameter] = value
        return this
    }

    fun withPathParameters(pathParameters: MutableMap<String, String>): HttpEventBuilder {
        this.pathParameters = pathParameters
        return this
    }

    fun withPathParameter(pathParameter: String, value: String): HttpEventBuilder {
        this.pathParameters[pathParameter] = value
        return this
    }

    fun withStageVariables(stageVariables: MutableMap<String, String>): HttpEventBuilder {
        this.stageVariables = stageVariables
        return this
    }

    fun withStageVariable(stageVariable: String, value: String): HttpEventBuilder {
        this.stageVariables[stageVariable] = value
        return this
    }

    fun withBody(body: String): HttpEventBuilder {
        this.body = body
        return this
    }

    fun withIsBase64Encoded(isBase64Encoded: Boolean): HttpEventBuilder {
        this.isBase64Encoded = isBase64Encoded
        return this
    }

    fun withRequestId(requestId: String): HttpEventBuilder {
        this.requestId = requestId
        return this
    }

    fun withAuthorizationContext(key: String, value: String): HttpEventBuilder {
        this.authorizationContext[key] = value
        return this
    }

    fun build(): HttpEvent {
        return HttpEvent(
            headers,
            queryStringParameters,
            pathParameters,
            stageVariables,
            authorizationContext,
            body,
            isBase64Encoded,
            requestId
        )
    }
}
