package testing.webserver.resources

import javax.servlet.http.HttpServletResponse

class StringResource(private val content: String, private val contentType: String): WebResource {

    override fun writeResponse(response: HttpServletResponse) {
        response.contentType = contentType
        response.status = HttpServletResponse.SC_OK

        response.writer.print(content)
        response.writer.close()
    }
}