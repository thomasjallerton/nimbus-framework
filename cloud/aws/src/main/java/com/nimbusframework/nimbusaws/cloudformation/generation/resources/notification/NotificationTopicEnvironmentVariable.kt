package com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedNotificationTopic
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition

class NotificationTopicEnvironmentVariable(
    notificationTopic: ParsedNotificationTopic
): NimbusEnvironmentVariable<ParsedNotificationTopic>(notificationTopic) {

    override fun getKey(): String {
        return "SNS_TOPIC_ARN_" + annotation.topicName.uppercase()
    }

}
