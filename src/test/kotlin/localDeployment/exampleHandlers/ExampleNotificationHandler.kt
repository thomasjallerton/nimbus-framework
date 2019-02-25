package localDeployment.exampleHandlers

import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.notification.UsesNotificationTopic
import localDeployment.exampleModels.Person

class ExampleNotificationHandler {

    @NotificationServerlessFunction(topic="test-topic")
    @UsesNotificationTopic(topic = "test-client-topic")
    fun receiveNotification(person: Person): Person {
        return person
    }
}