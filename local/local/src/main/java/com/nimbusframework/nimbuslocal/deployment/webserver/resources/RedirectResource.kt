package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class RedirectResource(private val redirectTarget: String): WebResource(arrayOf(), listOf("*"), "") {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {

        response.sendRedirect(redirectTarget)
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT

        response.outputStream.close()
    }
}
