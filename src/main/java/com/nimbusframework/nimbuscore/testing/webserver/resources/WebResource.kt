package com.nimbusframework.nimbuscore.testing.webserver.resources

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface WebResource {

    fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String)
}