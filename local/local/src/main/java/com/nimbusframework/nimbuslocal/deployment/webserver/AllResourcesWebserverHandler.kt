package com.nimbusframework.nimbuslocal.deployment.webserver

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.RedirectResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.WebConsole
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AllResourcesWebserverHandler(stage: String) : AbstractHandler() {

    private val handlerMap: MutableMap<String, WebServerHandler> = mutableMapOf()

    init {
        val webConsole = WebConsole(stage)
        handlerMap[""] = webConsole
        handlerMap["NimbusWebConsole"] = webConsole
    }

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {

        val pathPrefix = extractPathPrefix(target)

        handleCors(response)

        val handler = handlerMap[pathPrefix]
        if (handler != null) {
            val subTarget = target.substringAfter("/$pathPrefix")
            val subTargetSlash = if (subTarget.startsWith("/") || subTarget.isEmpty()) {
                subTarget
            } else {
                "/$subTarget"
            }
            handler.handle(subTargetSlash, baseRequest, request, response)
        } else {
            response.status = HttpServletResponse.SC_NOT_FOUND
            response.writer.close()

            baseRequest.isHandled = true
        }
    }

    fun handleCors(response: HttpServletResponse) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "")
    }

    fun addResource(pathPrefix: String, handler: WebServerHandler) {
        handlerMap[pathPrefix] = handler
        handler.addRedirectResource("", HttpMethod.GET, RedirectResource("$pathPrefix/"))
    }

    private fun extractPathPrefix(path: String): String {
        return path.substring(1).substringBefore("/")
    }
}