package testing.webserver.resources

import java.io.InputStream
import javax.servlet.http.HttpServletResponse

class InputStreamResource(
        private val inputStream: InputStream,
        private val contentType: String
): WebResource {

    override fun writeResponse(response: HttpServletResponse) {
        response.contentType = contentType
        response.status = HttpServletResponse.SC_OK

        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)

        response.outputStream.write(buffer)
        response.outputStream.close()
    }
}