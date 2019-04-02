package clients.websocket

import java.nio.ByteBuffer

class ServerlessFunctionWebsocketClientLocal: ServerlessFunctionWebSocketClient {

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {}

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {}

}