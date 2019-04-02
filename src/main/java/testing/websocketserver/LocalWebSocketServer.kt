package testing.websocketserver

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.websocket.api.Session
import testing.websocket.LocalWebsocketMethod

class LocalWebSocketServer(
        private val sessions: MutableMap<String, Session>
) {

    private val server: Server = Server()
    private var connectMethod: LocalWebsocketMethod? = null
    private var disconnectMethod: LocalWebsocketMethod? = null
    private val topics: MutableMap<String, LocalWebsocketMethod> = mutableMapOf()

    fun setup(port: Int) {
        val connector = ServerConnector(server)
        connector.port = port
        server.addConnector(connector)

        val handler = ServletContextHandler(ServletContextHandler.SESSIONS)
        handler.contextPath = "/"
        server.handler = handler

        val servletHolder = ServletHolder()
        servletHolder.servlet = WebSocketServlet(connectMethod, disconnectMethod, topics, sessions)

        handler.addServlet(servletHolder, "")
    }

    fun start() {
        server.start()
        server.join()
    }

    fun startWithoutJoin() {
        server.start()
    }

    fun addTopic(topic: String, method: LocalWebsocketMethod) {
        when (topic) {
            "\$connect" -> connectMethod = method
            "\$disconnect" -> disconnectMethod = method
            else -> topics[topic] = method
        }
    }
}