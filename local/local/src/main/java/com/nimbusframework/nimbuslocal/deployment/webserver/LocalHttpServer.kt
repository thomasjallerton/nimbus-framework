package com.nimbusframework.nimbuslocal.deployment.webserver

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler.gzip.GzipHandler

class LocalHttpServer(val port: Int, val webServerHandler: WebServerHandler) {

    val handler: AbstractHandler
    var server: Server? = null

    init {
        val gzipHandler = GzipHandler()
        gzipHandler.handler = CorsPassThroughHandler(webServerHandler)
        handler = gzipHandler
    }

    fun startServer() {
        val localServer = Server(port)
        val connector: Connector = ServerConnector(localServer)
        localServer.addConnector(connector)

        server = localServer

        localServer.handler = handler

        localServer.start()
        localServer.join()
    }

    fun startServerWithoutJoin() {
        val localServer = Server(port)
        server = localServer

        localServer.handler = handler

        localServer.start()
    }

    fun stopServer() {
        server?.stop()
        server = null
    }
}
