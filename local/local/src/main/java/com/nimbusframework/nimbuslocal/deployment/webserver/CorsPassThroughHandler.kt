package com.nimbusframework.nimbuslocal.deployment.webserver

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CorsPassThroughHandler(
    val handler: WebServerHandler
) : AbstractHandler() {

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        handleCors(response)

        val targetSlash = if (target.startsWith("/") || target.isEmpty()) {
            target
        } else {
            "/$target"
        }
        handler.handle(targetSlash, baseRequest, request, response)

    }

    fun handleCors(response: HttpServletResponse) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "")
    }

}
