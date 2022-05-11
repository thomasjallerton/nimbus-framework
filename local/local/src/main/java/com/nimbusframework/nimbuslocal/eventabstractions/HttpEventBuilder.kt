package com.nimbusframework.nimbuslocal.eventabstractions

import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import java.util.*

class HttpEventBuilder {

    private var resource: String? = null
    private var path: String? = null
    private var httpMethod: String? = null
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var multiValueHeaders: MutableMap<String, List<String>> = mutableMapOf()
    private var queryStringParameters: MutableMap<String, String> = mutableMapOf()
    private var multiValueQueryStringParameters: MutableMap<String, List<String>> = mutableMapOf()
    private var pathParameters: MutableMap<String, String> = mutableMapOf()
    private var stageVariables: MutableMap<String, String> = mutableMapOf()
    private var authorizationContext: MutableMap<String, Any> = mutableMapOf()
    private var body: String? = null
    private var isBase64Encoded: Boolean? = null
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

    fun withMultiValueHeaders(multiValueHeaders: MutableMap<String, List<String>>): HttpEventBuilder {
        this.multiValueHeaders = multiValueHeaders
        return this
    }

    fun withMultiValueHeader(multiValueHeader: String, value: List<String>): HttpEventBuilder {
        this.multiValueHeaders[multiValueHeader] = value
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

    fun withMultiValueQueryStringParameters(multiValueQueryStringParameters: MutableMap<String, List<String>>): HttpEventBuilder {
        this.multiValueQueryStringParameters = multiValueQueryStringParameters
        return this
    }

    fun withMultiValueQueryStringParameter(multiValueQueryStringParameter: String, value: List<String>): HttpEventBuilder {
        this.multiValueQueryStringParameters[multiValueQueryStringParameter] = value
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

    fun build(): HttpEvent {
        return HttpEvent(
            resource,
            path,
            httpMethod,
            headers,
            multiValueHeaders,
            queryStringParameters,
            multiValueQueryStringParameters,
            pathParameters,
            stageVariables,
            authorizationContext,
            body,
            isBase64Encoded,
            requestId
        )
    }
}
