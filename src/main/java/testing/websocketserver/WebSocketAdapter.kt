package testing.websocketserver

import org.eclipse.jetty.server.SessionIdManager
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import testing.websocket.LocalWebsocketMethod
import testing.websocket.WebSocketRequest
import wrappers.websocket.models.RequestContext
import java.util.*


class WebSocketAdapter(
        private var connectMethod: LocalWebsocketMethod?,
        private var disconnectMethod: LocalWebsocketMethod?,
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
        connectMethod?.invoke(request)

        super.onWebSocketConnect(session)
        sessions[sessionId] = session
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        val request = WebSocketRequest(
                "",
                parameters,
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
                    parameters,
                    headers,
                    RequestContext("MESSAGE", sessionId)
            )
            val topic = request.getTopic()
            topics[topic]?.invoke(request)
        }
        super.onWebSocketText(message)
    }
}