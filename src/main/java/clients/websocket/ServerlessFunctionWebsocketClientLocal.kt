package clients.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import testing.LocalNimbusDeployment
import java.nio.ByteBuffer

class ServerlessFunctionWebsocketClientLocal: ServerlessFunctionWebSocketClient {

    private val localDeployment = LocalNimbusDeployment.getInstance()
    private val sessions = localDeployment.getWebSocketSessions()
    private val objectMapper = ObjectMapper()

    override fun sendToConnection(connectionId: String, data: ByteBuffer) {
        val session = sessions[connectionId]
        session?.remote?.sendBytes(data)
    }

    override fun sendToConnectionConvertToJson(connectionId: String, data: Any) {
        val session = sessions[connectionId]
        val json = objectMapper.writeValueAsString(data)

        session?.remote?.sendString(json)
    }
}