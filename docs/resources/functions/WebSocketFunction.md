---
id: WebSocketFunction
title: WebSocket Function
sidebar_label: WebSocket Function
---

WebSocket functions allow you to build up a WebSocket API for realtime communication with com.nimbusframework.nimbuscore.clients. You can only have one WebSocket API in a nimbus project (i.e. only one endpoint).

## Basic Usage
Using the `WebSocketServerlessFunction` com.nimbusframework.nimbuscore.annotation a topic where the function will be triggered is specified. There are three important topics, these are:

* `$default` - Used when the topic selection expression produces a value that does not match any of the other route keys in your API routes. This can be used, for example, to implement a generic error handling mechanism.
* `$connect` - The associated route is used when a client first connects to your WebSocket API.
* `$disconnect` - The associated route is used when a client disconnects from your API. This call is made on a best-effort basis.

The route selection is done via the `request.body.topic` expression. This means to select what topic is triggered the message send from the client must contain a field called `"topic"` where its value is the topic. For example to trigger a function that is listening to the newUser topic a message like this would have to be sent: 
```json
{
  "topic": "newUser",
  "username": "user",
  "password": "1234"
}
``` 

A corresponding backend function for this could look like:
```java
class WebSocketHandlers {
    
    @WebSocketServerlessFunction(topic="newUser")
    public void newUser(NewUser newUser, WebSocketEvent event) {
        String connectionId = event.getRequestContext().getConnectionId();
        System.out.println("ConnectionId " + connectionId + " registered as new user: " + newUser);
    }
    
    class NewUser {
        public String username;
        public String password;
    }
}
```

## Method Details
### Parameters
A WebSocket serverless function method can have at most two parameters. One of these is a `WebSocketEvent` type, which contains the header and query parameters, as well as the connection id. The second method parameter available is a custom user type which will be read and deserialized from the JSON body of the request. This is done using the jackson library, so any customisation you want can be done using jackson annotations in the target class. An example with both parameters is shown above.

The order of the parameters supplied to the method does not matter, and one or both can be left out.

### Return type
The return type is unused in the cloud, instead to send a message back to the client you should use the [ServerlessFunctionWebSocketClient](../../clients/WebSocketServerlessFunctionClient.md). This class allows you to send messages to a ConnectionId. It is important to note that messages cannot be sent to a client until after the `$connect` function has returned. 

#### Connection Method Details
To accept a client connection the function must return successfully (i.e. no exceptions). If you want to refuse a client connection this function must throw an exception (this is caught by nimbus and translated to a refused connection). You also cannot sent messages to a client until this method has returned successfully. 

#### Example WebChat Server

Here is an example WebChat server implemented using nimbus. It is very basic, allowing users to send messages to each other. 

```java
public class WebSocketApi {

    private ServerlessFunctionWebSocketClient webSocketClient = ClientBuilder.getServerlessFunctionWebSocketClient();
    private DocumentStoreClient<UserDetail> userDetails = ClientBuilder.getDocumentStoreClient(UserDetail.class);
    private KeyValueStoreClient<String, ConnectionDetail> connectionDetails = ClientBuilder.getKeyValueStoreClient(String.class, ConnectionDetail.class);

    @WebSocketServerlessFunction(topic = "$connect")
    @UsesDocumentStore(dataModel = UserDetail.class)
    @UsesKeyValueStore(dataModel = ConnectionDetail.class)
    public void onConnect(WebSocketEvent event) throws Exception {
        String connectionId = event.getRequestContext().getConnectionId();
        String username = event.getQueryStringParameters().get("user");
        UserDetail validUser = userDetails.get(username);
        if (validUser != null) {
            ConnectionDetail connectionDetail = new ConnectionDetail(username);
            System.out.println("Adding " + connectionDetail.getUsername() + " with connection " + connectionId);
            connectionDetails.put(connectionId, connectionDetail);
            if (validUser.getCurrentWebsocket() != null) {
                connectionDetails.delete(validUser.getCurrentWebsocket());
            }
            validUser.setCurrentWebsocket(connectionId);
            userDetails.put(validUser);
        } else {
            throw new Exception("Not a valid user");
        }
    }

    @WebSocketServerlessFunction(topic = "$disconnect")
    @UsesDocumentStore(dataModel = UserDetail.class)
    @UsesKeyValueStore(dataModel = ConnectionDetail.class)
    public void onDisconnect(WebSocketEvent event) {
        String connectionId = event.getRequestContext().getConnectionId();
        ConnectionDetail disconnectedUser = connectionDetails.get(connectionId);
        if (disconnectedUser != null) {
            UserDetail validUser = userDetails.get(disconnectedUser.getUsername());
            if (validUser != null) {
                validUser.setCurrentWebsocket(null);
                userDetails.put(validUser);
            }
        }
        connectionDetails.delete(event.getRequestContext().getConnectionId());
    }

    @WebSocketServerlessFunction(topic = "sendMessage")
    @UsesServerlessFunctionWebSocketClient
    @UsesDocumentStore(dataModel = UserDetail.class)
    @UsesKeyValueStore(dataModel = ConnectionDetail.class)
    public void onMessage(WebSocketMessage message, WebSocketEvent event) {
        UserDetail userDetail = userDetails.get(message.getRecipient());
        ConnectionDetail connectionDetail = connectionDetails.get(event.getRequestContext().getConnectionId());
        if (userDetail != null && connectionDetail != null) {
            System.out.println();
            webSocketClient.sendToConnectionConvertToJson(userDetail.getCurrentWebsocket(),
                    new Message(message.getMessage(), connectionDetail.getUsername()));
        }
    }

    @AfterDeployment
    @UsesDocumentStore(dataModel = UserDetail.class)
    public void setupBasicUsers() {
        UserDetail thomas = new UserDetail("thomas", null);
        UserDetail bob = new UserDetail("bob", null);

        userDetails.put(thomas);
        userDetails.put(bob);
    }
}
``` 

It is recommended to keep track of connection ids that one of the stores are used. 

## Deployment Information
Many of these methods can be written to build up a WebSocket api. Once it has been deployed the base URL will be reported. 

Any logging lines or print statements will appear in the cloud providers log service (e.g. AWS CloudWatch).

## Annotation Specification
### @WebSocketServerlessFunction
#### Required parameters
* `topic` - The topic that will trigger the function 

#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.