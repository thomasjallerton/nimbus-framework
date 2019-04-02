package testing.webserver.resources

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RedirectResource(private val target: String): WebResource {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse) {

        response.sendRedirect(target)
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT

        response.outputStream.close()
    }
}