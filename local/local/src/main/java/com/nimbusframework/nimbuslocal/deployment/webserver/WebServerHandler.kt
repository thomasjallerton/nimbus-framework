package com.nimbusframework.nimbuslocal.deployment.webserver

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuslocal.deployment.http.HttpMethodIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.LocalHttpMethod
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.FileResource
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.FunctionResource
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.File

open class WebServerHandler(
    private val indexFile: String,
    private val errorFile: String
): AbstractHandler() {

    private val resources: MutableMap<HttpMethodIdentifier, WebResource> = mutableMapOf()
    private var errorResource: WebResource? = null

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val httpMethod = HttpMethod.valueOf(request.method)
        var webResource: WebResource? = null

        for ((identifier, resource) in resources) {
            if (identifier.matches(target, httpMethod)) {
                webResource = resource
                break
            }
        }

        if (webResource != null) {
            if (httpMethod == HttpMethod.OPTIONS) {
                // Preflight CORS request
                webResource.addAllowedHeaders(response)
                response.status = HttpServletResponse.SC_OK
                response.writer.close()
            } else if (passesCors(webResource, request)) {
                webResource.addAllowedHeaders(response)
                webResource.writeResponse(request, response, target)
                if (webResource.gzipResponse) {
                    // The jetty GzipHandler only uses gzip if the content length is >= 32 bytes
                    response.addHeader("Content-Length", "32")
                }
            } else {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.writer.println("CORS prevented access to this resource ($target). Check logs for more details.")
                response.writer.close()
            }
        } else {
            if (errorResource != null) {
                errorResource!!.writeResponse(request, response, target)
            } else {
                response.status = HttpServletResponse.SC_NOT_FOUND
                response.writer.close()
            }
        }
        baseRequest.isHandled = true
    }

    fun addWebResource(path: String, file: File, contentType: String, allowedOrigins: List<String>) {
        addNewResource(path, HttpMethod.GET, FileResource(file, contentType, allowedOrigins, combinePaths(path)))
    }

    fun addWebResource(path: String, httpMethod: HttpMethod, method: LocalHttpMethod, allowedOrigin: String, allowedHeaders: Array<String>, gzipResponse: Boolean) {
        addNewResource(path, httpMethod, FunctionResource(path, httpMethod, method, allowedHeaders, allowedOrigin, combinePaths(path), gzipResponse))
    }

    private fun addNewResource(path: String, httpMethod: HttpMethod, webResource: WebResource) {
        val fixedPath = if (path.isNotEmpty()) "/$path" else path

        resources[HttpMethodIdentifier(fixedPath, httpMethod)] = webResource
        if (path == indexFile) {
            resources[HttpMethodIdentifier("/", HttpMethod.GET)] = webResource
        }
        if (path == errorFile) {
            errorResource = webResource
        }
    }

    private fun passesCors(webResource: WebResource, request: HttpServletRequest): Boolean {
        val headersToCheck = request.getHeader("Access-Control-Request-Headers")?.split(",")
        //Check Access-Control-Request-Headers headers
        if (headersToCheck != null) {
            if (!webResource.checkCorsHeaders(headersToCheck)) return false
        }

        //Check origin
        val referer = request.getHeader("Referer")
        if (referer != null) {
            return webResource.checkCorsOrigin(referer)
        }
        return true
    }

    private fun combinePaths(path: String): String {
        return when {
            path.startsWith("/") -> path
            else -> "/$path"
        }
    }
}
