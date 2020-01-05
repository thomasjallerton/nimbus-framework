package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic

class UsesNotificationTopicAnnotation(private val notificationTopicAnnotation: UsesNotificationTopic): DataModelAnnotation() {

    override val stages = notificationTopicAnnotation.stages

    override fun internalDataModel(): Class<out Any> {
        return notificationTopicAnnotation.notificationTopic.java
    }

}