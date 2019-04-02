package testing.websocket

import com.fasterxml.jackson.databind.DeserializationFeature
import testing.ServerlessMethod
import wrappers.http.models.HttpEvent
import wrappers.websocket.models.RequestContext
import wrappers.websocket.models.WebSocketEvent
import java.lang.reflect.Method

class LocalWebsocketMethod(private val method: Method, private val invokeOn: Any) : ServerlessMethod(method, HttpEvent::class.java) {

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    fun invoke(request: WebSocketRequest): Any? {
        timesInvoked++

        val webSocketEvent = WebSocketEvent(
                body = request.body,
                headers = request.headers,
                queryStringParameters = request.queryStringParams,
                requestContext = RequestContext("", "testConnection")
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
                method.invoke(invokeOn, webSocketEvent, param)
            } else {
                val param = objectMapper.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, webSocketEvent)
            }
        } else if (params.size == 1) {
            if (eventIndex == 0) {
                method.invoke(invokeOn, webSocketEvent)
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
}