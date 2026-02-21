package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import com.nimbusframework.nimbuscore.annotations.http.HttpException
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.HttpResponse
import com.nimbusframework.nimbuscore.exceptions.HttpJsonErrorMessage
import com.nimbusframework.nimbuscore.persisted.userconfig.HttpErrorMessageType
import com.nimbusframework.nimbuscore.persisted.userconfig.UserConfig
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.http.HttpMethodIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.deployment.http.LocalHttpMethod
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*

class FunctionResource(
    private val path: String,
    private val httpMethod: HttpMethod,
    private val method: LocalHttpMethod,
    private val httpErrorMessageType: HttpErrorMessageType,
    allowedHeaders: Array<String>,
    allowedOrigin: String,
    baseRequest: String,
    gzipResponse: Boolean
) : WebResource(allowedHeaders, listOf(allowedOrigin), baseRequest, gzipResponse) {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val byteArrayBody = request.inputStream.readBytes()
        val (strBody, isBase64Encoded) = if (request.getHeader("Content-Encoding") == "gzip") {
            Pair(String(Base64.getEncoder().encode(byteArrayBody)), true)
        } else {
            Pair(String(byteArrayBody), false)
        }
        val (headersV1, headersV2) = getHeaders(request)

        val correctedPath = if (request.queryString.isNullOrEmpty()) {
            target
        } else {
            "$target?${request.queryString}"
        }

        val httpRequestV2 = HttpRequest(correctedPath, httpMethod, strBody, headersV2, isBase64Encoded)
        val httpRequestV1 = HttpRequest(correctedPath, httpMethod, strBody, headersV1, isBase64Encoded)

        try {
            val authResponse = LocalNimbusDeployment.getInstance().localResourceHolder.httpAuthenticator?.allow(httpRequestV1)
            if (authResponse?.authenticated == false) {
                throw HttpException(403, "Unauthenticated")
            }
            val result = method.invoke(httpRequestV2, HttpMethodIdentifier(path, httpMethod), authResponse?.context ?: mapOf())

            response.contentType = "application/json"

            if (result is HttpResponse) {
                for ((headerName, headerValue) in result.headers) {
                    response.setHeader(headerName, headerValue)
                }
                response.status = result.statusCode

                response.writer.print(result.body)
                response.writer.flush()
                response.writer.close()
            } else if (result is ByteArray) {
                response.contentType = "application/octet-stream"
                response.outputStream.write(result)
                response.outputStream.flush()
                response.outputStream.close()
            } else if (result !is Unit) {
                response.writer.print(JacksonClient.writeValueAsString(result))
                response.writer.flush()
                response.writer.close()
            }
        } catch (e: HttpException) {
            handleHttpException(response, e)
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            e.printStackTrace()
        }
    }

    private fun handleHttpException(response: HttpServletResponse, e: HttpException) {
        response.status = e.statusCode
        if (e.logException) {
            e.printStackTrace()
        }
        when (httpErrorMessageType) {
            HttpErrorMessageType.NONE -> {
                response.contentType = "text/plain"
            }
            HttpErrorMessageType.PLAIN_TEXT -> {
                response.contentType = "text/plain"
                response.writer.print(e.message)
            }
            HttpErrorMessageType.APPLICATION_JSON -> {
                response.contentType = "application/json"
                response.writer.print(JacksonClient.writeValueAsString(HttpJsonErrorMessage(e.message ?: "Error")))
            }
            HttpErrorMessageType.HEADER -> {
                response.contentType = "text/plain"
                response.setHeader("Nimbus-Error-Message", e.message)
            }
        }
    }

    private fun getHeaders(request: HttpServletRequest): Pair<Map<String, List<String>>, Map<String, List<String>>> {
        val headerNames = request.headerNames
        val resultV1: MutableMap<String, List<String>> = mutableMapOf()
        val resultV2: MutableMap<String, List<String>> = mutableMapOf()
        for (headerName in headerNames) {
            val headerVal = request.getHeader(headerName)
            resultV1[headerName] = headerVal.split(",")
            resultV2[headerName.lowercase()] = headerVal.split(",")
        }
        return Pair(resultV1, resultV2)
    }

}
