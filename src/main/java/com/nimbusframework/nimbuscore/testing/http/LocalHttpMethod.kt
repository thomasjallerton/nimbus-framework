package com.nimbusframework.nimbuscore.testing.http

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpEvent
import java.lang.reflect.Method
import java.net.URL
import java.net.URLDecoder
import java.util.LinkedList
import java.util.LinkedHashMap


class LocalHttpMethod(private val method: Method, private val invokeOn: Any) : ServerlessMethod(method, HttpEvent::class.java) {

    fun invoke(request: HttpRequest, methodIdentifier: HttpMethodIdentifier): Any? {
        timesInvoked++

        val multiValueQueryStringParameters = extractMultiValueQueryStringParameters(request.path)
        val queryStringParameters = multiValueParamsToSingle(multiValueQueryStringParameters)

        val httpEvent = HttpEvent(
                pathParameters = methodIdentifier.extractPathParameters(request.path),
                headers = multiValueParamsToSingle(request.headers),
                multiValueHeaders = request.headers,
                queryStringParameters = queryStringParameters,
                multiValueQueryStringParameters = multiValueQueryStringParameters
        )

        val strParam = request.body
        val eventIndex = eventIndex()
        val params = method.parameters
        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (eventIndex == 0) {
                val param = objectMapper.readValue(strParam, params[1].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, httpEvent, param)
            } else {
                val param = objectMapper.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, httpEvent)
            }
        } else if (params.size == 1) {
            if (eventIndex == 0) {
                method.invoke(invokeOn, httpEvent)
            } else {
                val param = objectMapper.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
        return mostRecentValueReturned
    }

    private fun extractMultiValueQueryStringParameters(path: String): Map<String, List<String>> {
        val queryPairs: MutableMap<String, MutableList<String>> = mutableMapOf()
        val pairs = path.substringAfter("?").split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            val key = if (idx > 0) URLDecoder.decode(pair.substring(0, idx), "UTF-8") else pair
            if (!queryPairs.containsKey(key)) {
                queryPairs[key] = mutableListOf()
            }
            val value = if (idx > 0 && pair.length > idx + 1) URLDecoder.decode(pair.substring(idx + 1), "UTF-8") else "null"
            queryPairs[key]!!.add(value)
        }
        return queryPairs
    }

    private fun multiValueParamsToSingle(params: Map<String, List<String>>): Map<String, String> {
        return params.mapValues {(_, value) -> value.fold("") {
            acc, s -> if (acc.isNotEmpty()) "$acc,s" else s } }
    }
}