package testing.webserver

import annotation.annotations.function.HttpMethod
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import testing.webserver.resources.RedirectResource
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AllResourcesWebserverHandler : AbstractHandler() {

    private val handlerMap: MutableMap<String, WebserverHandler> = mutableMapOf()

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
        handler.addNewResource("", HttpMethod.GET, RedirectResource("$pathPrefix/"))
    }

    private fun extractPathPrefix(path: String): String {
        return path.substring(1).substringBefore("/")
    }
}