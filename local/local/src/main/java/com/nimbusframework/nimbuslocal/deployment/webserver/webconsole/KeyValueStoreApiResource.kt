package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.StoreInformation
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.TableItems
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class KeyValueStoreApiResource(private val httpMethod: HttpMethod, private val stage: String) : WebResource(arrayOf(), listOf(), "") {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val tables = localNimbusDeployment.localResourceHolder.webKeyValueStores

                when (operation) {
                    "listTables" -> {
                        val listOfTables = tables.map { (_, localBucket) ->
                            StoreInformation(
                                    localBucket.internalTableName,
                                    localBucket.size()
                            )
                        }
                        val tablesJson = JacksonClient.writeValueAsString(listOfTables)
                        response.outputStream.bufferedWriter().use {it.write(tablesJson) }
                    }
                    "listTableItems" -> {
                        val tableName = request.getParameter("tableName")
                        val client = tables["$tableName$stage"]
                        if (client != null) {
                            val itemDescription = client.getItemDescription()
                            val items = client.getAll()
                            val itemList = items.map { (key, value) ->
                                val jsonObject = JacksonClient.readTree(JacksonClient.writeValueAsString(value)) as ObjectNode
                                jsonObject.put(itemDescription.key, JacksonClient.writeValueAsString(key))
                                jsonObject
                            }
                            val tableItemsJson = JacksonClient.writeValueAsString(TableItems(itemList, itemDescription))
                            response.outputStream.bufferedWriter().use {it.write(tableItemsJson) }
                        } else {
                            println("CLIENT WAS NULL FOR $tableName$stage")
                        }
                    }
                }

            }
            HttpMethod.POST -> {
                val tableName = request.getParameter("tableName")
                val client = localNimbusDeployment.localResourceHolder.webKeyValueStores["$tableName$stage"]
                if (client != null) {

                    when (operation) {
                        "newItem" -> {
                            val newItem = request.inputStream.bufferedReader().use { it.readText() }
                            client.putJson(newItem)
                        }
                        "deleteItem" -> {
                            val deleteItem = request.inputStream.bufferedReader().use { it.readText() }
                            client.deleteJson(deleteItem)
                        }
                    }
                }
            }
        }
    }
}
