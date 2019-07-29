package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.function.ServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.information.WebSocketFunctionInformation
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod
import java.lang.reflect.Method

class LocalWebSocketFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val webSocketPort: Int,
        private val variableSubstitution: MutableMap<String, String>,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val webSocketServerlessFunctions = method.getAnnotationsByType(WebSocketServerlessFunction::class.java)
        if (webSocketServerlessFunctions.isEmpty()) return false

        for (webSocketFunction in webSocketServerlessFunctions) {
            if (webSocketFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val webSocketMethod = LocalWebsocketMethod(method, invokeOn)
                val functionInformation = WebSocketFunctionInformation(webSocketFunction.topic)

                localResourceHolder.websocketMethods[webSocketFunction.topic] = webSocketMethod
                localResourceHolder.functions[functionIdentifier] = ServerlessFunction(
                        webSocketMethod,
                        functionInformation
                )
                localResourceHolder.webSocketServer.addTopic(webSocketFunction.topic, webSocketMethod)

                variableSubstitution["\${NIMBUS_WEBSOCKET_API_URL}"] = "ws://localhost:$webSocketPort"
            }
        }
        return true
    }

}