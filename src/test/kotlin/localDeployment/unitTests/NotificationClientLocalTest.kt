package localDeployment.unitTests

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import localDeployment.exampleHandlers.ExampleNotificationHandler
import localDeployment.exampleModels.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NotificationClientLocalTest {

    @Test
    fun subscribingWorksAndSendingMessageWorks() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic("test-client-topic")

        val localClient = ClientBuilder.getNotificationClient("test-client-topic")

        localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.notify("message")

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(1, sentMessages.size)
        assertEquals("message", sentMessages[0])
    }

    @Test
    fun subscribingWorksAndSendingMessageWorksWithObject() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic("test-client-topic")
        val person = Person("Thomas", 21)

        val localClient = ClientBuilder.getNotificationClient("test-client-topic")

        localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.notifyJson(person)

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(1, sentMessages.size)
        assertEquals(person, sentMessages[0])
    }

    @Test
    fun unsubscribeWorks() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic("test-client-topic")

        val localClient = ClientBuilder.getNotificationClient("test-client-topic")

        val id = localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.deleteSubscription(id)
        localClient.notify("message")

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(0, sentMessages.size)
    }
}