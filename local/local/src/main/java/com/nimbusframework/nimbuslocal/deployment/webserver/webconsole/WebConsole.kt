package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.deployment.http.HttpMethodIdentifier
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.InputStreamResource
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.RedirectResource
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import org.eclipse.jetty.server.Request
import java.lang.Exception
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import javax.servlet.http.HttpServletResponse.SC_OK


class WebConsole(stage: String) : WebServerHandler("", "") {

    private val apis: MutableMap<HttpMethodIdentifier, WebResource> = mutableMapOf()

    init {
        apis[HttpMethodIdentifier("/FileBucketAPI", HttpMethod.POST)] = FileBucketApiResource(HttpMethod.POST)
        apis[HttpMethodIdentifier("/FileBucketAPI", HttpMethod.GET)] = FileBucketApiResource(HttpMethod.GET)
        apis[HttpMethodIdentifier("/FileBucketAPI", HttpMethod.OPTIONS)] = FileBucketApiResource(HttpMethod.OPTIONS)
        apis[HttpMethodIdentifier("/DocumentStoreAPI", HttpMethod.POST)] = DocumentStoreApiResource(HttpMethod.POST, stage)
        apis[HttpMethodIdentifier("/DocumentStoreAPI", HttpMethod.GET)] = DocumentStoreApiResource(HttpMethod.GET, stage)
        apis[HttpMethodIdentifier("/DocumentStoreAPI", HttpMethod.OPTIONS)] = DocumentStoreApiResource(HttpMethod.OPTIONS, stage)
        apis[HttpMethodIdentifier("/KeyValueStoreAPI", HttpMethod.POST)] = KeyValueStoreApiResource(HttpMethod.POST, stage)
        apis[HttpMethodIdentifier("/KeyValueStoreAPI", HttpMethod.GET)] = KeyValueStoreApiResource(HttpMethod.GET, stage)
        apis[HttpMethodIdentifier("/KeyValueStoreAPI", HttpMethod.OPTIONS)] = KeyValueStoreApiResource(HttpMethod.OPTIONS, stage)
        apis[HttpMethodIdentifier("/QueueAPI", HttpMethod.POST)] = QueueApiResource(HttpMethod.POST)
        apis[HttpMethodIdentifier("/QueueAPI", HttpMethod.GET)] = QueueApiResource(HttpMethod.GET)
        apis[HttpMethodIdentifier("/QueueAPI", HttpMethod.OPTIONS)] = QueueApiResource(HttpMethod.OPTIONS)
        apis[HttpMethodIdentifier("/NotificationAPI", HttpMethod.POST)] = NotificationApiResource(HttpMethod.POST)
        apis[HttpMethodIdentifier("/NotificationAPI", HttpMethod.GET)] = NotificationApiResource(HttpMethod.GET)
        apis[HttpMethodIdentifier("/NotificationAPI", HttpMethod.OPTIONS)] = NotificationApiResource(HttpMethod.OPTIONS)
        apis[HttpMethodIdentifier("/FunctionAPI", HttpMethod.POST)] = FunctionApiResource(HttpMethod.POST)
        apis[HttpMethodIdentifier("/FunctionAPI", HttpMethod.GET)] = FunctionApiResource(HttpMethod.GET)
        apis[HttpMethodIdentifier("/FunctionAPI", HttpMethod.OPTIONS)] = FunctionApiResource(HttpMethod.OPTIONS)

        apis[HttpMethodIdentifier("/", HttpMethod.GET)] = RedirectResource("/NimbusWebConsole/index.html")
        apis[HttpMethodIdentifier("", HttpMethod.GET)] = RedirectResource("/NimbusWebConsole/index.html")
    }

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val method = HttpMethod.valueOf(request.method)
        val handler = apis[HttpMethodIdentifier(target, method)]
        if (handler != null) {
            handler.writeResponse(request, response, target)
            response.status = SC_OK
            response.setHeader("Access-Control-Allow-Origin", "*")
            response.outputStream.close()
        } else {
            if (method == HttpMethod.GET) {
                //Attempt to load the file
                val webResourceOpt = loadWebConsoleFile(target)
                if (webResourceOpt.isPresent) {
                    webResourceOpt.get().writeResponse(request, response, target)
                    response.status = SC_OK
                    response.setHeader("Access-Control-Allow-Origin", "*")
                    response.outputStream.close()
                    return
                }
            }
            response.status = SC_NOT_FOUND
            response.outputStream.close()
        }

    }

    private fun loadWebConsoleFile(path: String): Optional<WebResource> {
        val contentType =
                when {
                    path.endsWith(".json") -> "application/json"
                    path.endsWith(".html") -> "text/html"
                    path.endsWith(".js") -> "text/javascript"
                    path.endsWith(".svg") -> "image/svg+xml"
                    path.endsWith(".ico") -> "image/vnd.microsoft.icon"
                    path.endsWith(".css") -> "text/css"
                    else -> "text/plain"
                }

        return try {
            val inputStream = javaClass.getResourceAsStream("/com/nimbusframework/nimbuslocal/webconsole/build$path")
            Optional.of(InputStreamResource(inputStream, contentType, listOf(), ""))
        } catch (e: Exception) {
            e.printStackTrace()
            Optional.empty()
        }
    }


}
