package com.nimbusframework.nimbuscore.testing.webserver.webconsole

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.webserver.resources.WebResource
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.FunctionInformation
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.NotificationInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FunctionApiResource(private val httpMethod: HttpMethod) : WebResource(arrayOf(), listOf(), "") {

    val objectMapper = ObjectMapper()


    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val functions = localNimbusDeployment.localResourceHolder.methods

                when (operation) {
                    "listFunctions" -> {
                        val listOfNotificationTopics = functions.map { (functionIdentifier, method) ->
                            FunctionInformation(
                                    functionIdentifier.className.substringAfterLast('.'),
                                    functionIdentifier.methodName,
                                    method.timesInvoked
                            )
                        }
                        val tablesJson = objectMapper.writeValueAsString(listOfNotificationTopics)
                        response.outputStream.bufferedWriter().use { it.write(tablesJson) }
                    }
                }

            }
            else -> {}}
        }
    }
