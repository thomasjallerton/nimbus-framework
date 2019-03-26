package testing.webserver

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import testing.webserver.resources.FileResource
import testing.webserver.resources.InputStreamResource
import testing.webserver.resources.StringResource
import testing.webserver.resources.WebResource
import java.io.File
import java.io.InputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WebserverHandler(private val indexFile: String,
                       private val errorFile: String
): AbstractHandler() {

    private val resources: MutableMap<String, WebResource> = mutableMapOf()
    private var errorResource: WebResource? = null

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val webResource = resources[target]
        if (webResource != null) {
            webResource.writeResponse(response)
        } else {
            if (errorResource != null) {
                errorResource!!.writeResponse(response)
            } else {
                response.status = HttpServletResponse.SC_NOT_FOUND
                response.writer.close()
            }
        }
        baseRequest.isHandled = true
    }

    fun addWebResource(path: String, file: File, contentType: String = "text/html") {
        addNewResource(path, FileResource(file, contentType))
    }

    fun addWebResource(path: String, content: String, contentType: String = "text/html") {
        addNewResource(path, StringResource(content, contentType))
    }

    fun addWebResource(path: String, inputStream: InputStream, contentType: String = "text/html") {
        addNewResource(path, InputStreamResource(inputStream, contentType))
    }

    private fun addNewResource(path: String, webResource: WebResource) {
        val fixedPath = "/$path"
        resources[fixedPath] = webResource
        if (path == indexFile) {
            resources["/"] = webResource
        }
        if (path == errorFile) {
            errorResource = webResource
        }
    }
}