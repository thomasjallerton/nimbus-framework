package com.nimbusframework.nimbuslocal.deployment.webserver.resources

import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FileResource(
        private val file: File,
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

        val initialStream = file.inputStream()
        val buffer = ByteArray(initialStream.available())
        initialStream.read(buffer)

        response.outputStream.write(buffer)
        response.outputStream.close()
    }

    companion object {
        private const val webConsolePath = "com/nimbusframework/nimbuscore/testing/webserver/webconsole"
        fun fromWebConsoleHTML(path: String): FileResource {
            return FileResource(
                    getResourceFile("$webConsolePath$path"),
                    "text/html",
                    listOf(),
                    "")
        }
        fun fromWebConsoleJS(path: String): FileResource {
            return FileResource(
                    getResourceFile("$webConsolePath$path"),
                    "text/babel",
                    listOf(),
                    "")
        }
        fun fromWebConsoleCSS(path: String): FileResource {
            return FileResource(
                    getResourceFile("$webConsolePath$path"),
                    "text/css",
                    listOf(),
                    "")
        }
        private fun getResourceFile(path: String): File {
            return File(FileResource::class.java.classLoader.getResource(path).file)
        }
    }
}