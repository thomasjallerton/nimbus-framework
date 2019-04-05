---
id: NotificationClient
title: Notification Topic Client
sidebar_label: Notification Topic Client
---

A notification topic client allows you to send notifications to the topic, as well as create and remove subscriptions. 

## Initialisation

To create a NotificationClient a topic name needs to be supplied, corresponding to the topic that you want to manage. 

```java
NotificationClient notificationClient = ClientBuilder.getNotificationClient(topicName);
```

Additionally any serverless function that uses a `NotificationClient` must also be annotated with `@UsesNotificationTopic(topicName)`. This needs to be given the name of the topic as well. This gives the function the correct cloud permissions and also will create the notification topic if it does not exist in the project.

In the above examples topicName should be replaced with the name of your topic.

## NotificationClient Methods
* `String createSubscription(Protocol protocol, String endpoint)` - Creates a subscription, where protocol could be `SMS` or `EMAIL`, and endpoint could be "example@nimbusframework.com" or "+441297468216". Returns the subscriptionId. Recommend using some kind of store to keep track of subscriptionIds.

* `notify(String message)` - Sends a notification to the topic, which will notify everyone subscribed.

* `notifyJson(Object message)` - Sends a notification to the topic, serializing the message provided into JSON. 
 
* `deleteSubscription(String subscriptionId)` - Unsubscribes subscription id from the notification topic, so they will no longer be notified.

## Annotation Specification
### @UsesNotificationTopic
#### Required Parameters
* `topic` - The name of the notification topic the function will interact with.
#### Optional Parameters
* `stages` - The stages that the function will be able to interact with the notification topic.