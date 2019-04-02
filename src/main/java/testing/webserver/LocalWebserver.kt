package testing.webserver

import org.eclipse.jetty.server.Server

class LocalWebserver {

    val handler = AllResourcesWebserverHandler()
    var server: Server? = null

    fun startServer(port: Int) {
        val localServer = Server(port)
        localServer.handler = handler

        localServer.start()
        localServer.join()

        server = localServer
    }

    fun stopServer() {
        server?.stop()
        server = null
    }
}