---
id: QueueFunction
title: Queue Function
sidebar_label: Queue Function
---

A queue function will be triggered when an item is added to a queue. Unlike a notification topic, consumers compete to with each other. This means that only one consumer will ingest the new item(s), unlike a notification where all consumers ar notified about a new object. 

## Basic Usage
A method is annotated with `@QueueServerlessFunction`, specifying a a queue id and a batch size. This batch size specifies the maximum amount of items an invocation of the serverless function will contain (not necessarily the annotated method). 

This is an example of a queue function:
```java
class QueueFunction {
    
    @QueueServerlessFunction(id = "messageQueue", batchSize = 1)
    public void consumeQueue(QueueItem item, QueueEvent event) {
        if (item.priority > 5) {
            System.out.println("GOT HIGH PRIORITY MESSAGE " + item.messageToProcess);
        }
        /* Additional Processing */
    }
    
    class QueueItem {
        public String messageToProcess;
        public int priority;
    }
}
```

This function will take an item off the queue, if it fails then the item will be put back onto the queue and the function will retry, or the queue will consume it. 

## Method Details
### Parameters
The annotated method can have at most two parameters. There are two cases, if the batchSize is set to one, or more than one. 

* batchSize is one. If batch size of one, then one parameter is a custom user type that is deserialized from queue message, and one is of type `QueueEvent`. An example of this case is shown above.

* batchSize is more than one. Here one parameter is a custom user type that is deserialized from queue message, and one is of type `QueueEvent` OR one parameter is a **List** of custom user types and the second is a **List** of `QueueEvents`. In the first case, nimbus calls your function multiple times with each parameter pair. In the second, the list of parameters and events are passed directly to your function. It is guaranteed that both lists are of the same size, and that index i in both correspond to the same item. 

An example of both of these more than one cases is shown below: 

```java
class QueueFunction {
    
    @QueueServerlessFunction(id = "messageQueue", batchSize = 10)
    public void consumeQueue(QueueItem item, QueueEvent event) {
        if (item.priority > 5) {
            System.out.println("GOT HIGH PRIORITY MESSAGE " + item.messageToProcess);
        }
        /* Additional Processing */
    }
    
    @QueueServerlessFunction(id = "messageQueue", batchSize = 10)
    public void consumeQueue(List<QueueItem> items, List<QueueEvent> events) {
        boolean foundHighPriority = false;
        StringBuilder highPriorityMessages = "";
        for (QueueItem item : items) {
            if (item.priority > 5) {
                foundHighPriority = true;
                highPriorityMessages += item.messageToProcess + " ";
            }
        }
        if (foundHighPriority) System.out.println("Found high priority messages " + highPriorityMessages);
        /* Additional Processing */
    }
    
    class QueueItem {
        public String messageToProcess;
        public int priority;
    }
}
```

### Return type
The return type is not used for anything and will be ignored in the deployed function. In local unit tests you can still access any returned value.

## Annotation Specification
### @QueueServerlessFunction
#### Required Parameters
* `batchSize` - How many items per serverless function invocation
* `id` - Name of the queue


#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.