package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import com.nimbusframework.nimbuscore.annotations.http.CorsInformation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

abstract class WebResource(
        headers: Array<String>,
        private val allowedOrigins: List<String>,
        private val baseRequest: String,
        val gzipResponse: Boolean
) {

    private val allowedHeaders: Set<String>

    init {
        val allowedHeadersTmp = CorsInformation.allowedHeaders.toMutableSet()
        headers.forEach { allowedHeadersTmp.add(it.lowercase()) }
        allowedHeaders = allowedHeadersTmp
    }

    abstract fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String)

    fun checkCorsOrigin(referer: String): Boolean {
        if (allowedOrigins.contains("*")) return true

        if (allowedOrigins.size == 1) {
            if (allowedOrigins[0] == "" && !referer.startsWith(baseRequest)) {
                println("WARNING: CORS Exception occurred. Access-Control-Allow-Origin does not permit $referer")
                return false
            }
        }

        for (allowedOrigin in allowedOrigins) {
            if (!referer.startsWith(allowedOrigin)) {
                println("WARNING: CORS Exception occurred. Access-Control-Allow-Origin does not permit $referer")
                return false
            }
        }
        return true
    }

    fun checkCorsHeaders(headersToCheck: List<String>): Boolean {
        for (header in headersToCheck) {
            if (!allowedHeaders.contains(header)) {
                println("WARNING: CORS Exception occurred. Access-Control-Allow-Headers did not contain $header")
                return false
            }
        }
        return true
    }

    fun addAllowedHeaders(response: HttpServletResponse) {
        response.addHeader("Access-Control-Allow-Headers", allowedHeaders.joinToString())
    }
}
