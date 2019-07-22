package com.nimbusframework.nimbuscore.testing.webserver.webconsole

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.webserver.resources.WebResource
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.NotificationInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NotificationApiResource(private val httpMethod: HttpMethod) : WebResource(arrayOf(), listOf(), "") {

    val objectMapper = ObjectMapper()


    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val queues = localNimbusDeployment.localResourceHolder.notificationTopics

                when (operation) {
                    "listNotificationTopics" -> {
                        val listOfNotificationTopics = queues.map { (topicName, topic) ->
                            NotificationInformation(
                                    topicName,
                                    topic.getNumberOfSubscribers(),
                                    topic.generalSubscribers.map { (_, subscriberInformation) -> subscriberInformation },
                                    topic.getFunctionSubscribers(),
                                    topic.getTotalNumberOfNotifications()
                            )
                        }
                        val tablesJson = objectMapper.writeValueAsString(listOfNotificationTopics)
                        response.outputStream.bufferedWriter().use { it.write(tablesJson) }
                    }
                }

            }
            HttpMethod.POST -> {
                val topicName = request.getParameter("topicName")
                val notificationTopic = localNimbusDeployment.localResourceHolder.notificationTopics[topicName]
                if (notificationTopic != null) {

                    when (operation) {
                        "notify" -> {
                            val message = request.inputStream.bufferedReader().use { it.readText() }
                            notificationTopic.notifyJson(message)
                        }
                    }
                }
            }
            else -> {}}
        }
    }
