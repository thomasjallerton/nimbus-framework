package com.nimbusframework.nimbuscore.testing.webserver.webconsole

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import com.nimbusframework.nimbuscore.testing.webserver.resources.WebResource
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.StoreInformation
import com.nimbusframework.nimbuscore.testing.webserver.webconsole.models.TableItems
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class KeyValueStoreApiResource(private val httpMethod: HttpMethod, private val stage: String) : WebResource(arrayOf(), listOf(), "") {

    private val objectMapper = ObjectMapper()

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val tables = localNimbusDeployment.localResourceHolder.keyValueStores

                when (operation) {
                    "listTables" -> {
                        val listOfTables = tables.map { (_, localBucket) ->
                            StoreInformation(
                                    localBucket.internalTableName,
                                    localBucket.size()
                            )
                        }
                        val tablesJson = objectMapper.writeValueAsString(listOfTables)
                        response.outputStream.bufferedWriter().use {it.write(tablesJson) }
                    }
                    "listTableItems" -> {
                        val tableName = request.getParameter("tableName")
                        val client = tables["$tableName$stage"]
                        if (client != null) {
                            val itemDescription = client.getItemDescription()
                            val items = client.getAll()
                            val objectMapper = ObjectMapper()
                            val itemList = items.map { (key, value) ->
                                val jsonObject = objectMapper.readTree(objectMapper.writeValueAsString(value)) as ObjectNode
                                jsonObject.put(itemDescription.key, objectMapper.writeValueAsString(key))
                                jsonObject
                            }
                            val tableItemsJson = objectMapper.writeValueAsString(TableItems(itemList, itemDescription))
                            response.outputStream.bufferedWriter().use {it.write(tableItemsJson) }
                        } else {
                            println("CLIENT WAS NULL FOR $tableName$stage")
                        }
                    }
                }

            }
            HttpMethod.POST -> {
                val tableName = request.getParameter("tableName")
                val client = localNimbusDeployment.localResourceHolder.keyValueStores["$tableName$stage"]
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