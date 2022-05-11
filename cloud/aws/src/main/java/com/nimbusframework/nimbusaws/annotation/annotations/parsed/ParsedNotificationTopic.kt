package com.nimbusframework.nimbusaws.annotation.annotations.parsed

import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition

data class ParsedNotificationTopic(
    val topicName: String,
    val stages: Array<String> = arrayOf()
): ParsedAnnotation {

    constructor(notificationTopicDefinition: NotificationTopicDefinition): this(
        notificationTopicDefinition.topicName,
        notificationTopicDefinition.stages
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParsedNotificationTopic) return false

        if (topicName != other.topicName) return false
        if (!stages.contentEquals(other.stages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topicName.hashCode()
        result = 31 * result + stages.contentHashCode()
        return result
    }

}
