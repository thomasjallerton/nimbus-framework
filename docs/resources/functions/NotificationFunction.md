---
id: NotificationFunction
title: Notification Function
sidebar_label: Notification Function
---

A notification function is one which will be triggered whenever an item is posted to a notification topic. There can be multiple listeners to the topic, each which will be triggered when a new item is posted. 

## Basic usage
A method is annotated with `@NotificationServerlessFunction`, with a topic provided. This will create a notification topic if it does not already exist within the project. 

This is a basic example: 

```java
public class NotificationHandler {
    
    @NotificationServerlessFunction(topic="newParty")
    public gotNotification(Party newParty, NotificationEvent event) {
        System.out.println("Got new party notification " + newParty);
    }
    
    class Party {
        public String name;
        public String location;
    }
}
```

The `NotificationEvent` parameter gives you more details about the notification, like the timestamp and any additional parameters. 

As with all serverless functions, a notification function must be inside of a class with a default constructor available. 

## Method Details
### Parameters 
A notification function can have at most two parameters. One is a custom user type that is deserialized from the message in the notification, the second is the `NotificationEvent` parameter. An example is shown above. The deserialization is done using the jackson library, so any customisation you want can be done using jackson annotations in the target class. 

The ordering of the parameters does not matter, and one or both of the parameters can be left out.
### Return type
The return type is not used for anything and will be ignored in the deployed function. In local unit tests you can still access any returned value.

## Annotation details
### @NotificationServerlessFunction
#### Required Parameters
* `topic` - The notification topic the function will listen to, and will be triggered on any new data. 

#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.

