package com.nimbusframework.nimbuscore.testing.webserver.resources

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.amazonaws.util.IOUtils
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.testing.http.HttpRequest
import com.nimbusframework.nimbuscore.testing.http.LocalHttpMethod
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.HashMap



class FunctionResource(
        private val path: String,
        private val httpMethod: HttpMethod,
        private val method: LocalHttpMethod
): WebResource {

    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
    }

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse) {
        val strBody = IOUtils.toString(request.inputStream)
        val queryStringParams = getQueryParameters(request)
        val headers = getHeaders(request)

        val httpRequest = HttpRequest(path, httpMethod, strBody, mutableMapOf(), queryStringParams, headers)

        val result = method.invoke(httpRequest)

        response.contentType = "application/json"

        if (result is HttpResponse) {
            for ((headerName, headerValue) in result.headers) {
                response.setHeader(headerName, headerValue)
            }
            response.status = result.statusCode

            response.writer.print(result.body)
        } else if (result !is Unit){
            response.writer.print(objectMapper.writeValueAsString(result))
        }
        response.writer.close()
    }

    private fun getQueryParameters(request: HttpServletRequest): Map<String, String> {
        val queryParameters = HashMap<String, String>()

        if (request.queryString.isNullOrEmpty()) return queryParameters

        val queryString = request.queryString

        if (queryString.isEmpty()) {
            return queryParameters
        }

        val parameters = queryString.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (parameter in parameters) {
            val keyValuePair = parameter.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            queryParameters[keyValuePair[0]] = keyValuePair[1]
        }
        return queryParameters
    }

    private fun getHeaders(request: HttpServletRequest): Map<String, String> {
        val headerNames = request.headerNames
        val result: MutableMap<String, String> = mutableMapOf()

        for (headerName in headerNames) {
            result[headerName] = request.getHeader(headerName)
        }
        return result
    }

}