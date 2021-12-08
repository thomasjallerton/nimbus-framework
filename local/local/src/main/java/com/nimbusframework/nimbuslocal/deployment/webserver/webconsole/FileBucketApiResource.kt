package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.FileBucketInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT
import javax.servlet.MultipartConfigElement



class FileBucketApiResource(private val httpMethod: HttpMethod) : WebResource(arrayOf(), listOf(), "") {

    private val objectMapper = ObjectMapper()

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                when (operation) {
                    "listBuckets" -> {
                        val buckets = localNimbusDeployment.localResourceHolder.fileStorage

                        val listOfBuckets = buckets.map {(bucketName, localBucket) ->
                            FileBucketInformation(
                                    bucketName,
                                    localBucket.listFiles().size,
                                    localBucket.configuredAsStaticWebsite
                            )
                        }
                        val bucketsJson = objectMapper.writeValueAsString(listOfBuckets)
                        response.outputStream.bufferedWriter().use {it.write(bucketsJson) }
                    }
                    "getFile" -> {
                        val bucketName = request.getParameter("bucketName")
                        val client = localNimbusDeployment.getLocalFileStorage(bucketName)
                        val destinationPath = request.getParameter("destinationPath")
                        val inputStream = client.getFile(destinationPath)
                        response.contentType = "application/octet-stream"
                        inputStream.copyTo(response.outputStream)
                        inputStream.close()
                    }
                    "listFiles" -> {
                        val bucketName = request.getParameter("bucketName")
                        val client = localNimbusDeployment.getLocalFileStorage(bucketName)
                        val files = client.listFiles()
                        val filesJson = objectMapper.writeValueAsString(files)
                        response.outputStream.bufferedWriter().use { it.write(filesJson) }
                    }
                }

            }
            HttpMethod.POST -> {
                val bucketName = request.getParameter("bucketName")
                val client = localNimbusDeployment.getLocalFileStorage(bucketName)

                when (operation) {
                    "saveFile" -> {
                        val multipartConfigElement = MultipartConfigElement(null as String?)
                        request.setAttribute(__MULTIPART_CONFIG_ELEMENT, multipartConfigElement)
                        val destinationPath = request.getParameter("destinationPath")
                        client.saveFile(destinationPath, request.getPart("file").inputStream)
                    }
                    "deleteFile" -> {
                        val destinationPath = request.getParameter("destinationPath")

                        client.deleteFile(destinationPath)
                    }
                }

            }
        }
    }
}