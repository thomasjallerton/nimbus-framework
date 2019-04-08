---
id: UnitTests
title: Unit Testing
sidebar_label: Unit Testing
---

The nimbus framework provides some useful features to test that your functions work correctly by simulating the cloud environment. This allows you to check that a chain of function triggers is what you expect, that your functions are triggered with the correct output, and that stores have the items you expect in them.

## Basics

All unit tests that you want run in a simulated cloud environment rely on the `NimbusLocalDeployment` class. This class provides all the functionality required to test your project. Here is an example of a simple unit test.

The data model used in the example:
```java
@DocumentStore
class Person {
    @Key
    public String email;
    @Attribute
    public String fullName;
    
    public Person() {}
    
    public Person(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }
    
    //Relevant equals, hashCode etc. methods. 
    ...
}
```

The code we want to test:
```java
class RestHandlers {
   
    private DocumentStoreClient<Person> client = ClientBuilder.getDocumentStoreClient(Person.class);
    
    @HttpServerlessFunction(method = HttpMethod.POST, path = "person")
    @UsesDocumentStore(Person.class)
    public String storePerson(Person person) {
        client.put(person);
        return "Successfully saved person";
    }
}
```

The unit test (assuming both files are part of the "com.allerton.nimbusExample.testExample" package):
```java
class TestRestHandlers {
    
    @Test
    public void testStorePersonPutsItemIntoStore() {
        LocalNimbusDeployment localNimbusDeployment = LocalNimbusDeployment.getNewInstance("com.allerton.nimbusExample.testExample");
        
        ServerlessMethod storePersonMethod = localNimbusDeployment.getMethod(RestHandlers.class, "storePerson");
        
        assertEquals(0, storePersonMethod.getTimesInvoked());
        
        Person newPerson = new Person("testEmail", "test person");
        
        HttpRequest testRequest = new HttpRequest("person", HttpMethod.POST);
        testRequest.setBodyFromObject(newPerson);
        
        localNimbusDeployment.sendHttpRequest(testRequest); //Blocking call, simulates environment until reaches a constant state
        
        assertEquals(1, storePersonMethod.getTimesInvoked()); //Check the function was invoked
        
        LocalDocumentStore<Person> localStore = localNimbusDeployment.getDocumentStore(Person.class);
        
        assertEquals(1, localStore.size());
        
        assertEquals(newPerson, localStore.get("testEmail"));
    }
}
```

This test function checks that our function is called on the correct HTTP request, and that it does put the item into the store.

## Instantiating LocalNimbusDeployment
There are a few ways to get a `LocalNimbusDeployment` object. These are:

`LocalNimbusDeployment.getNewInstance(Class<T> object)` - Creates and simulates resources found only in this class, completely new instance.
`LocalNimbusDeployment.getInstance(String packageName)` - Creates and simulates resources found in the package, looking in all subpackages. Also a completely new instance.
`LocalNimbusDeployment.getInstance()` - Returns an existing LocalNimbusDeployment object. This is used by the nimbus com.nimbusframework.nimbuscore.clients primarily to interact with the local deployment. Not recommended for general use.

## LocalNimbusDeployment Methods for Unit Tests

* `<K, V> LocalKeyValueStore<K, V> getKeyValueStore(Class<V> valueClass)` - Gets the local deployment of the key value store corresponding to valueClass. LocalKeyValueStore has similar API to KeyValueStoreClient.

* `<T> LocalDocumentStore<T> getDocumentStore(Class<T>: documentStore)` - Gets the local deployment of the document store corresponding to documentStore. LocalDocumentStore has similar API to DocumentStoreClient.

* `LocalFileStorage getLocalFileStorage(String bucketName)` - Gets the local deployment of the file storage bucket corresponding to bucketName. LocalFileStorage has a similar API to FileStorageClient. 

* `LocalQueue getQueue(String id)` - Gets the local deployment of the queue corresponding to the queue id. LocalQueue allows you to add items to the queue, one at a time or in a batch. You can also query how many items have been added to the queue.
 
* `LocalNotificationTopic getNotificationTopic(String topic)` - Gets the local deployment of the notification topic corresponding to topic. LocalNotificationTopic allows you to send notifications directly, as well as querying what endpoints have been notified with (as no emails or SMSs will be sent).
    
* `<T> ServerlessMethod getMethod(Class<T> handlerClass, String methodName)` - Returns a wrapper for a serverless method that you have written, allowing you to check number of invocations as well as the most recent arguments and return values. 

* `void sendHttpRequest(HttpRequest request)` - Send a HTTP request internally (i.e. no actual HTTP request sent) which will trigger any functions set up to respond to the specified path and method. 

* `void connectToWebSockets(Map<String, String> headers, Map<String, String> queryStringParams)` - Send a connection request to the WebSocket API (i.e. sends to the $connect topic).

* `void disconnectFromWebSockets()` - Send a disconnect request to the WebSocket API (i.e. along the $disconnect topic).

* `sendWebSocketRequest(WebSocketRequest request)` - Send a request which will trigger any WebSocket function set to listen to the specified topic.

These, along with the usual ClientBuilder.getClient() methods should allow for extensive unit com.nimbusframework.nimbuscore.testing of your project. 

