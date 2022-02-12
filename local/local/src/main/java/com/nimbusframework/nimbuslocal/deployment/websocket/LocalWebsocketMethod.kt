package com.nimbusframework.nimbuslocal.deployment.websocket

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import java.lang.reflect.Method

class LocalWebsocketMethod(
        private val method: Method,
        private val invokeOn: Any
) : ServerlessMethod(method, WebSocketEvent::class.java, FunctionType.WEBSOCKET) {

    fun invoke(request: WebSocketRequest): Any? {
        timesInvoked++

        val webSocketEvent = WebSocketEvent(
                body = request.body,
                headers = request.headers,
                queryStringParameters = request.queryStringParams,
                requestContext = request.requestContext
        )

        val strParam = request.body
        val eventIndex = eventIndex()
        val params = method.parameters
        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (eventIndex == 0) {
                val param = JacksonClient.readValue(strParam, params[1].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, webSocketEvent, param)
            } else {
                val param = JacksonClient.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, webSocketEvent)
            }
        } else if (params.size == 1) {
            if (eventIndex == 0) {
                method.invoke(invokeOn, webSocketEvent)
            } else {
                val param = JacksonClient.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
        return mostRecentValueReturned
    }
}
