package com.nimbusframework.nimbuslocal.deployment.webserver

import org.eclipse.jetty.server.Server

class LocalHttpServer(stage: String) {

    val handler = AllResourcesWebserverHandler(stage)
    var server: Server? = null

    fun startServer(port: Int) {
        val localServer = Server(port)
        server = localServer

        localServer.handler = handler

        localServer.start()
        localServer.join()
    }

    fun startServerWithoutJoin(port: Int) {
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