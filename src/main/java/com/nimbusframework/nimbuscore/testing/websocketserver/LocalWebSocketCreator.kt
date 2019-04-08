package com.nimbusframework.nimbuscore.testing.websocketserver

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse
import org.eclipse.jetty.websocket.servlet.WebSocketCreator
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod

class LocalWebSocketCreator(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
        private val topics: Map<String, LocalWebsocketMethod>,
        private val sessions: MutableMap<String, Session>
): WebSocketCreator {

    override fun createWebSocket(req: ServletUpgradeRequest?, resp: ServletUpgradeResponse?): Any? {
        if (req != null) {
            val headers = req.headers.mapValues {
                (_, values) -> values.joinToString(", ")
            }
            val parameters = req.parameterMap.mapValues {
                (_, values) -> values.joinToString(", ")
            }

            return WebSocketAdapter(
                    connectMethod,
                    disconnectMethod,
                    topics,
                    headers,
                    parameters,
                    sessions
            )
        }
        return null
    }
}