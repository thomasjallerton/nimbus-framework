package com.nimbusframework.nimbuscore.clients.websocket

import com.nimbusframework.nimbuscore.clients.PermissionException
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