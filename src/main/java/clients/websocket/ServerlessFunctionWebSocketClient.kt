package clients.websocket

import java.nio.ByteBuffer

interface ServerlessFunctionWebSocketClient {

    fun sendToConnection(connectionId: String, data: ByteBuffer)

    fun sendToConnectionConvertToJson(connectionId: String, data: Any)

}