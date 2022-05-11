package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.permissions.PermissionType
import java.nio.ByteBuffer

class ServerlessFunctionWebsocketClientLocal: ServerlessFunctionWebSocketClient, LocalClient(PermissionType.WEBSOCKET_MANAGER) {

    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, "")
    }

    override val clientName: String = ServerlessFunctionWebSocketClient::class.java.simpleName

    private val sessions = localNimbusDeployment.getWebSocketSessions()

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        checkClientUse()
        val session = sessions[connectionId]
        session?.remote?.sendBytes(data)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        checkClientUse()
        val session = sessions[connectionId]
        val json = JacksonClient.writeValueAsString(data)

        session?.remote?.sendString(json)
    }
}
