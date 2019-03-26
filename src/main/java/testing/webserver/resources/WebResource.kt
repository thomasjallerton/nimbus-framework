package testing.webserver.resources

import javax.servlet.http.HttpServletResponse

interface WebResource {

    fun writeResponse(response: HttpServletResponse)
}