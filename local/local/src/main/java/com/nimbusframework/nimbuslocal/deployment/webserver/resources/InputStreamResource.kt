package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import java.io.InputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class InputStreamResource(
        private val inputStream: InputStream,
        private val contentType: String,
        allowedOrigins: List<String>,
        baseRequest: String
): WebResource(
        arrayOf(),
        allowedOrigins,
        baseRequest
) {


    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        response.contentType = contentType
        response.status = HttpServletResponse.SC_OK

        inputStream.copyTo(response.outputStream)

        response.outputStream.close()
    }


}