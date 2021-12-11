package com.nimbusframework.nimbuslocal.deployment.webserver.webconsole

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.webserver.resources.WebResource
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.FunctionInformation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class FunctionApiResource(private val httpMethod: HttpMethod) : WebResource(arrayOf(), listOf(), "") {

    override fun writeResponse(request: HttpServletRequest, response: HttpServletResponse, target: String) {
        val localNimbusDeployment = LocalNimbusDeployment.getInstance()
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Headers", "content-type")
        val operation = request.getParameter("operation")

        when (httpMethod) {
            HttpMethod.GET -> {
                val functions = localNimbusDeployment.localResourceHolder.functions

                when (operation) {
                    "listFunctions" -> {
                        val listOfFunctionInformation = functions.map { (functionIdentifier, function) ->
                            FunctionInformation(
                                    functionIdentifier.className.substringAfterLast('.'),
                                    functionIdentifier.className,
                                    functionIdentifier.methodName,
                                    function.serverlessMethod.timesInvoked,
                                    function.serverlessMethod.type
                            )
                        }
                        val tablesJson = JacksonClient.writeValueAsString(listOfFunctionInformation)
                        response.outputStream.bufferedWriter().use { it.write(tablesJson) }
                    }
                    "functionInformation" -> {
                        val className = request.getParameter("className")
                        val methodName = request.getParameter("methodName")

                        val functionInformation = functions[FunctionIdentifier(className, methodName)]!!.functionInformation
                        val functionInformationJson = JacksonClient.writeValueAsString(functionInformation)
                        response.outputStream.bufferedWriter().use { it.write(functionInformationJson) }
                    }
                }

            }
            else -> {
            }
        }
    }
}
