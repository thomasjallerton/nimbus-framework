---
id: LocalServer
title: Local Server
sidebar_label: Local Server
---

For the times when unit tests are not enough, nimbus allows you to run a more extensive local deployment so you can locally interact with your REST and WebSocket APIs or your static websites.

## Basic Usage

Again we use the LocalNimbusDeployment class. You create a new instance and then call a start server function. The constructor is similar to how it was described in the [Unit Tests](UnitTests.md) documentation, however you now may want to specify the HTTP port and WebSocket port. These default to 8080 and 8081, respectively. 

Here is the function we want to deploy:
```java
class RestHandlers {
     
    @HttpServerlessFunction(method = HttpMethod.GET, path = "testEndpoint")
    public String helloWorld() {
        return "Hello World!";
    }
}
```

The code to run a local server is:
```java
class TestRestHandlers {
    
    @Test
    public void testStorePersonPutsItemIntoStore() {
        LocalNimbusDeployment localNimbusDeployment = LocalNimbusDeployment.getNewInstance(RestHandlers.class);
        
        localNimbusDeployment.startAllWebservers();
    }
}
```

This will start up a webserver, accessible at `http://localhost:8080`. The endpoint for this function will be `http://localhost:8080/function/testEndpoint`.

## LocalNimbusDeployment Methods for Local Servers
* `void startWebSocketServer()` - Starts a WebSocket server on the port specified in the constructor, or default 8081. Blocking call.

* `void startWebserver(String bucketName)` - Starts a HTTP server on the port specified in the constructor, or default 8080. Only hosts the static website that is held in the file storage bucket specified by bucketName. Blocking call.

* `void startServerlessFunctionWebserver()` - Starts a HTTP server on the port specified in the constructor, or default 8080. Only hosts endpoints specified by `@HttpServerlessFunction`. Blocking call.

* `void startAllWebservers()` - Starts a HTTP server on the port specified in the constructor, or default 8080. Hosts both static websites from file storage buckets and serverless functions. Blocking call.
    
* `startAllServers()` - Starts two servers, HTTP on 8080 (or the port specified in constructor) and WebSocket on 8081 (or the port specified in the constructor). For the HTTP will hosts both static websites from file storage buckets and serverless functions. Blocking call.

## Endpoint Specification
### HTTP
#### Function endpoints (from @HttpServerlessFunction)
These endpoints will be `http://localhost:HTTP_PORT/function/FUNCTION_ENDPOINT`
* `HTTP_PORT` is either 8080 or the port specified in the constructor. 
* `FUNCTION_ENDPOINT` is the endpoint specified in the @HttpServerlessFunction annotation.

#### Static Website (from @FileStorageBucket)
These endpoints will be `http://localhost:HTTP_PORT/BUCKET_NAME/FILE_PATH`
* `HTTP_PORT` is either 8080 or the port specified in the constructor. 
* `BUCKET_NAME` is the name of the bucket as specified in the @FileStorageBucket annotation.
* `FILE_PATH` is the path to the file in the bucket. If an index file is specified can be left blank to access this file.

### WebSocket
#### Function endpoints (from @WebSocketServerlessFunction)
This endpoint is `ws://localhost:WEBSOCKET_PORT`
* `WEBSOCKET_PORT` is either 8081 or the port specified in the constructor.
