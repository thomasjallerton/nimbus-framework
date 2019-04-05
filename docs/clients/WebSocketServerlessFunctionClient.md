---
id: WebSocketServerlessFunctionClient
title: WebSocket Management Client
sidebar_label: WebSocket Management Client
---

A `ServerlessFunctionWebSocketClient` allows a `@WebSocketServerlessFunction` to send messages along websockets to ConnectionIds. 

## Initialisation

To create a `ServerlessFunctionWebSocketClient`:

```java
ServerlessFunctionWebSocketClient webSocketClient = ClientBuilder.getServerlessFunctionWebSocketClient();
```

Additionally the serverless function that needs access to the client needs to be annotated with `@UsesServerlessFunctionWebSocketClient` to handle cloud permissions. 

## UsesServerlessFunctionWebSocketClient Methods

* `void sendToConnection(Sting connectionId, ByteBuffer data)` - Sends the contents of data to the client corresponding to the connectionId.

* `void sendToConnectionConvertToJson(String connectionId: String, Object data)` - Sends the contents of data to the client corresponding to the connectionId. This function handles the conversion to ByteBuffer for you.

## Annotation Sepecification
### @UsesServerlessFunctionWebSocketClient
#### Optional Parameters
* `stages` - The stages which this function has access to the websocket client


