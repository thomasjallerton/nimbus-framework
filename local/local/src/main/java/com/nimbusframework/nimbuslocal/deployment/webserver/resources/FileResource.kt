package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.nio.file.Files

class FileResource(
        private val file: File,
        private val contentType: String,
        allowedOrigins: List<String>,
        baseRequest: String
): WebResource(
        arrayOf(),
        allowedOrigins,
        baseRequest,
    true
) {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        response.contentType = contentType
        response.status = HttpServletResponse.SC_OK

        val contentLength = Files.size(file.toPath())
        response.setHeader("Content-Length", contentLength.toString())

        Files.copy(file.toPath(), response.outputStream)
        response.outputStream.close()
    }

}
