package com.nimbusframework.nimbuscore.testing.webserver.resources

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RedirectResource(private val redirectTarget: String): WebResource {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {

        response.sendRedirect(redirectTarget)
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT

        response.outputStream.close()
    }
}