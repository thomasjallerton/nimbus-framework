package com.nimbusframework.nimbuslocal.webserver.console

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import org.junit.Ignore
import org.junit.Test

class Server {

    @Test
    fun startServer() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(
                ""
        )
        localDeployment.startAllServers()
    }
}