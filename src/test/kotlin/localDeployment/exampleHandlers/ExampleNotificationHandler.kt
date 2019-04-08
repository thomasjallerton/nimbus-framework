package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.notification.UsesNotificationTopic
import localDeployment.exampleModels.Person

class ExampleNotificationHandler {

    @NotificationServerlessFunction(topic="test-topic")
    @UsesNotificationTopic(topic = "test-client-topic")
    fun receiveNotification(person: Person): Person {
        return person
    }
}