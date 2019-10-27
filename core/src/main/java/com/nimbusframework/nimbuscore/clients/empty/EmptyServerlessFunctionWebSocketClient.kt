package com.nimbusframework.nimbuscore.clients.empty

import com.nimbusframework.nimbuscore.clients.websocket.ServerlessFunctionWebSocketClient
import com.nimbusframework.nimbuscore.exceptions.PermissionException
import java.nio.ByteBuffer

class EmptyServerlessFunctionWebSocketClient: ServerlessFunctionWebSocketClient {
    private val clientName = "ServerlessFunctionWebSocketClient"

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        throw PermissionException(clientName)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        throw PermissionException(clientName)
    }
}