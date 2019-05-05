package com.nimbusframework.nimbuscore.testing.webserver.resources

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.amazonaws.util.IOUtils
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.testing.http.HttpMethodIdentifier
import com.nimbusframework.nimbuscore.testing.http.HttpRequest
import com.nimbusframework.nimbuscore.testing.http.LocalHttpMethod
import com.nimbusframework.nimbuscore.wrappers.http.models.HttpResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.HashMap



class FunctionResource(
        private val path: String,
        private val httpMethod: HttpMethod,
        private val method: LocalHttpMethod,
        allowedHeaders: Array<String>,
        allowedOrigin: String,
        baseRequest: String
): WebResource(allowedHeaders, listOf(allowedOrigin), baseRequest) {

    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
    }

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val strBody = IOUtils.toString(request.inputStream)
        val headers = getHeaders(request)

        val correctedPath = if (request.queryString.isNullOrEmpty()) {
            target
        } else {
            "$target?${request.queryString}"
        }

        val httpRequest = HttpRequest(correctedPath, httpMethod, strBody, headers)

        val result = method.invoke(httpRequest, HttpMethodIdentifier(path, httpMethod))

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

    private fun getHeaders(request: HttpServletRequest): Map<String, List<String>> {
        val headerNames = request.headerNames
        val result: MutableMap<String, List<String>> = mutableMapOf()
        for (headerName in headerNames) {
            val headerVal = request.getHeader(headerName)
            result[headerName] = headerVal.split(",")
        }
        return result
    }

}