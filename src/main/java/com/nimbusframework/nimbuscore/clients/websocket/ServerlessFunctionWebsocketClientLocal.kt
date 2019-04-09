package com.nimbusframework.nimbuscore.clients.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.clients.PermissionException
import com.nimbusframework.nimbuscore.clients.rdbms.DatabaseClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import java.nio.ByteBuffer

class ServerlessFunctionWebsocketClientLocal: ServerlessFunctionWebSocketClient, LocalClient() {

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.WEBSOCKET_MANAGER, "")
    }

    override val clientName: String = ServerlessFunctionWebSocketClient::class.java.simpleName

    private val sessions = localNimbusDeployment.getWebSocketSessions()
    private val objectMapper = ObjectMapper()

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        checkClientUse()
        val session = sessions[connectionId]
        session?.remote?.sendBytes(data)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        checkClientUse()
        val session = sessions[connectionId]
        val json = objectMapper.writeValueAsString(data)

        session?.remote?.sendString(json)
    }
}