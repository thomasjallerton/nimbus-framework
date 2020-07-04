package com.nimbusframework.nimbuscore.examples.queue;

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition;
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition;

@QueueDefinition(queueId = "queueId", stages = {"dev"})
public class Queue {}
