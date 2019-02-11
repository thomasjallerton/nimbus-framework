package testing

import wrappers.http.models.HttpEvent
import java.lang.Exception
import java.lang.reflect.Method

class HttpMethod(private val method: Method, private val invokeOn: Any): ServerlessMethod() {

    fun invoke(request: HttpRequest) {
        timesInvoked++

        val httpEvent = HttpEvent(
                pathParameters = request.pathParameters,
                headers = request.headers,
                queryStringParameters = request.queryStringParams
        )

        val strParam = if (request.body != null) {
            objectMapper.writeValueAsString(request.body)
        } else {
            "null"
        }

        val params = method.parameters
        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (params[0].type.canonicalName.contains(HttpEvent::class.java.canonicalName)) {
                val param = objectMapper.readValue(strParam, params[1].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, httpEvent, param)
            } else {
                val param = objectMapper.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, httpEvent)
            }
        } else if (params.size == 1) {
            if (params[0].type.canonicalName.contains(HttpEvent::class.java.canonicalName)) {
                method.invoke(invokeOn, httpEvent)
            } else {
                val param = objectMapper.readValue(strParam, params[0].type)
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
    }
}