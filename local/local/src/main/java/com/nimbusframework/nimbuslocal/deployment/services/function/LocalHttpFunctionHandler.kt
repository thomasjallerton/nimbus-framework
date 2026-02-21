package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.persisted.userconfig.HttpErrorMessageType
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment.Companion.functionWebserverIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.HttpFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.http.HttpMethodIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.LocalHttpMethod
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.webserver.LocalHttpServer
import com.nimbusframework.nimbuslocal.deployment.webserver.WebServerHandler
import java.lang.reflect.Method

class LocalHttpFunctionHandler(
    private val localResourceHolder: LocalResourceHolder,
    private val httpPort: Int,
    private val httpErrorMessageType: HttpErrorMessageType,
    private val variableSubstitution: MutableMap<String, String>,
    private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val httpServerlessFunctions = method.getAnnotationsByType(HttpServerlessFunction::class.java)
        if (httpServerlessFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val enabledRequestCompression = httpServerlessFunctions.any { it.enableRequestDecoding }
        val enabledResponseCompression = httpServerlessFunctions.any { it.enableResponseEncoding }

        val annotation = stageService.annotationForStage(httpServerlessFunctions) { annotation -> annotation.stages }
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val httpMethod = LocalHttpMethod(method, invokeOn, enabledRequestCompression)
            val functionInformation = HttpFunctionInformation(annotation.method, annotation.path)
            if (annotation.method != HttpMethod.ANY) {
                val httpIdentifier = HttpMethodIdentifier(annotation.path, annotation.method)
                localResourceHolder.httpMethods[httpIdentifier] = httpMethod

            } else {
                for (httpMethodType in HttpMethod.values()) {
                    val httpIdentifier = HttpMethodIdentifier(annotation.path, httpMethodType)
                    localResourceHolder.httpMethods[httpIdentifier] = httpMethod
                }
            }
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(httpMethod, functionInformation)

            val functionApiServer = localResourceHolder.httpServers.getOrPut(functionWebserverIdentifier) {
                variableSubstitution["\${NIMBUS_REST_API_URL}"] = "http://localhost:$httpPort"
                LocalHttpServer(httpPort, WebServerHandler("", ""))
            }
            val allowedOrigin = stageService.getDefaultAllowedOrigin()

            val allowedHeaders = stageService.getDefaultAllowedHeaders()

            functionApiServer.webServerHandler.addWebResource(
                annotation.path,
                annotation.method,
                httpMethod,
                allowedOrigin,
                allowedHeaders,
                enabledResponseCompression,
                httpErrorMessageType
            )
        }

        return true
    }
}
