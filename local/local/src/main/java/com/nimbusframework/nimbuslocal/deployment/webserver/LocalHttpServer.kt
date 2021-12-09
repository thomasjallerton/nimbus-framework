package com.nimbusframework.nimbuslocal.deployment.webserver

import org.eclipse.jetty.server.Server

class LocalHttpServer(val port: Int, handler: WebServerHandler) {

    val handler = CorsPassThroughHandler(handler)
    var server: Server? = null

    fun startServer() {
        val localServer = Server(port)
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
