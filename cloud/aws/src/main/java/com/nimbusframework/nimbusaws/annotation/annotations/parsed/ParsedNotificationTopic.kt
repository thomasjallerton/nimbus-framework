package com.nimbusframework.nimbusaws.annotation.annotations.parsed

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition

class ParsedNotificationTopic(
    val topicName: String,
    val stages: Array<String> = arrayOf()
): ParsedAnnotation {

    constructor(notificationTopicDefinition: NotificationTopicDefinition): this(
        notificationTopicDefinition.topicName,
        notificationTopicDefinition.stages
    )

}
