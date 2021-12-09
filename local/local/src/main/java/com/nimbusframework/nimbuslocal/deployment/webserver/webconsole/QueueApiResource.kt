package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.QueueInformation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class QueueApiResource(private val httpMethod: HttpMethod) : WebResource(arrayOf(), listOf(), "") {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val queues = localNimbusDeployment.localResourceHolder.queues

                when (operation) {
                    "listQueues" -> {
                        val listOfQueues = queues.map { (id, queue) ->
                            QueueInformation(
                                    id,
                                    queue.getNumberOfItemsAdded()
                            )
                        }
                        val tablesJson = JacksonClient.writeValueAsString(listOfQueues)
                        response.outputStream.bufferedWriter().use { it.write(tablesJson) }
                    }
                }

            }
            HttpMethod.POST -> {
                val queueName = request.getParameter("queueName")
                val queue = localNimbusDeployment.localResourceHolder.queues[queueName]
                if (queue != null) {

                    when (operation) {
                        "pushItem" -> {
                            val newItem = request.inputStream.bufferedReader().use { it.readText() }
                            queue.addJson(newItem)
                        }
                    }
                }
            }
            else -> {}}
        }
    }
