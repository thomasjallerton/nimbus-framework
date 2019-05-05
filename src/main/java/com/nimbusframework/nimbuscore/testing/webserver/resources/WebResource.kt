package com.nimbusframework.nimbuscore.testing.webserver.resources

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class WebResource(
        headers: Array<String>,
        private val allowedOrigins: List<String>,
        private val baseRequest: String
) {

    val allowedHeaders: Set<String>

    init {
        //Default CORs headers
        val allowedHeadersTmp = mutableSetOf(
                "accept",
                "accept-language",
                "content-language",
                "origin",
                "content-type"
        )
        headers.forEach { allowedHeadersTmp.add(it.toLowerCase()) }
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
}