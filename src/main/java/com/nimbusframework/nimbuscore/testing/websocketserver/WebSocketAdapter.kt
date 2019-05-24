package com.nimbusframework.nimbuscore.testing.websocketserver

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import com.nimbusframework.nimbuscore.testing.websocket.LocalWebsocketMethod
import com.nimbusframework.nimbuscore.testing.websocket.WebSocketRequest
import com.nimbusframework.nimbuscore.wrappers.websocket.models.RequestContext
import java.util.*


class WebSocketAdapter(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
        private var defaultMethod: LocalWebsocketMethod?,
        private val topics: Map<String, LocalWebsocketMethod>,
        private val headers: Map<String, String>,
        private val parameters: Map<String, String>,
        private val sessions: MutableMap<String, Session>
) : WebSocketAdapter() {

    private val sessionId = UUID.randomUUID().toString()

    override fun onWebSocketConnect(session: Session) {
        val request = WebSocketRequest(
                "",
                parameters,
                headers,
                RequestContext("CONNECT", sessionId)
        )
        if (connectMethod != null) {
            try {
                connectMethod!!.invoke(request)
            } catch (e: Exception) {
                return
            }
        }

        super.onWebSocketConnect(session)
        sessions[sessionId] = session
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        val request = WebSocketRequest(
                "",
                mapOf(),
                headers,
                RequestContext("DISCONNECT", sessionId)
        )
        disconnectMethod?.invoke(request)
        sessions.remove(sessionId)
        super.onWebSocketClose(statusCode, reason)
    }

    override fun onWebSocketText(message: String?) {
        if (message != null) {
            val request = WebSocketRequest(
                    message,
                    mapOf(),
                    headers,
                    RequestContext("MESSAGE", sessionId)
            )
            val topic = request.getTopic()
            if (topics.containsKey(topic)) {
                topics[topic]!!.invoke(request)
            } else {
                defaultMethod!!.invoke(request)
            }
        }
        super.onWebSocketText(message)
    }

    override fun onWebSocketBinary(payload: ByteArray, offset: Int, len: Int) {
        val message = String(payload, offset, len)
        onWebSocketText(message)
    }
}