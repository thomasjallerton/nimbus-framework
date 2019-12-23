package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.websocket.UsesServerlessFunctionWebSocket;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesWebSocketHandler {

    @WebSocketServerlessFunction(topic = "test")
    @UsesServerlessFunctionWebSocket()
    public void func() {
        ClientBuilder.getServerlessFunctionWebSocketClient();
    }

}
