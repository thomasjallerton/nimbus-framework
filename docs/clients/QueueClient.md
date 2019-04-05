---
id: QueueClient
title: Queue Client
sidebar_label: Queue Client
---

A queue client allows you to send messages to a queue.

## Initialisation

To create a QueueClient the queue id needs to be supplied, corresponding to the queue that you want to send messages to. 

```java
QueueClient client = ClientBuilder.getQueueClient(queueId);
```

Additionally any serverless function that uses a `QueueClient` must also be annotated with `@UsesQueue(queueId)`. This needs to be given the queue id as well. This gives the function the correct cloud permissions and also will create the queue if it does not exist in the project.

In the above examples queueId should be replaced with the id of your queue.

## QueueClient Methods
  
* `sendMessage(String message)` - Pushes the message onto the queue.

* `sendMessageAsJson(Object obj)` - Pushes the message onto the queue, serializing into JSON. 

## Annotation Specification
### @UsesQueue
#### Required Parameters
* `id` - The id of the queue the function will interact with.
#### Optional Parameters
* `stages` - The stages that the function will be able to interact with the queue.