package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.StoreInformation
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.TableItems
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class DocumentStoreApiResource(private val httpMethod: HttpMethod, private val stage: String) : WebResource(arrayOf(), listOf(), "") {

    private val objectMapper = ObjectMapper()

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val tables = localNimbusDeployment.localResourceHolder.webDocumentStores

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
                            val items = client.getAll()
                            val itemDescription = client.getItemDescription()
                            val tableItemsJson = objectMapper.writeValueAsString(TableItems(items, itemDescription))
                            response.outputStream.bufferedWriter().use {it.write(tableItemsJson) }
                        } else {
                            println("CLIENT WAS NULL FOR $tableName$stage")
                        }
                    }
                }

            }
            HttpMethod.POST -> {
                val tableName = request.getParameter("tableName")
                val client = localNimbusDeployment.localResourceHolder.webDocumentStores["$tableName$stage"]
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