package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.NotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.Person

class ExampleNotificationHandler {

    @NotificationServerlessFunction(notificationTopic=NotificationTopic::class)
    @UsesNotificationTopic(notificationTopic=NotificationTopic::class)
    fun receiveNotification(person: Person): Person {
        return person
    }
}