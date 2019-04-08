package com.nimbusframework.nimbuscore.testing.webserver

import org.eclipse.jetty.server.Server

class LocalWebserver {

    val handler = AllResourcesWebserverHandler()
    var server: Server? = null

    fun startServer(port: Int) {
        val localServer = Server(port)
        server = localServer

        localServer.handler = handler

        localServer.start()
        localServer.join()
    }

    fun stopServer() {
        server?.stop()
        server = null
    }
}