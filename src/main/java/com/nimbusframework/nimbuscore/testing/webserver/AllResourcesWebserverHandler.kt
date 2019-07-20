package com.nimbusframework.nimbuscore.testing.webserver

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import com.nimbusframework.nimbuscore.testing.webserver.resources.RedirectResource
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.WebConsole
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AllResourcesWebserverHandler(stage: String) : AbstractHandler() {

    private val handlerMap: MutableMap<String, WebserverHandler> = mutableMapOf()

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

    fun addResource(pathPrefix: String, handler: WebserverHandler) {
        handlerMap[pathPrefix] = handler
        handler.addRedirectResource("", HttpMethod.GET, RedirectResource("$pathPrefix/"))
    }

    private fun extractPathPrefix(path: String): String {
        return path.substring(1).substringBefore("/")
    }
}