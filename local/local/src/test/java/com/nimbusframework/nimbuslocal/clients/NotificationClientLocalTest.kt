package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleNotificationHandler
import com.nimbusframework.nimbuslocal.exampleModels.NotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions.assertEquals

class NotificationClientLocalTest: AnnotationSpec() {

    @Test
    fun subscribingWorksAndSendingMessageWorks() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic(NotificationTopic::class.java)

        val localClient = ClientBuilder.getNotificationClient(NotificationTopic::class.java)

        localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.notify("{\"name\":\"Tom\", \"age\":15}")

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(1, sentMessages.size)
        assertEquals("{\"name\":\"Tom\", \"age\":15}", sentMessages[0])
    }

    @Test
    fun subscribingWorksAndSendingMessageWorksWithObject() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic(NotificationTopic::class.java)
        val person = Person("Thomas", 21)

        val localClient = ClientBuilder.getNotificationClient(NotificationTopic::class.java)

        localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.notifyJson(person)

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(1, sentMessages.size)
        assertEquals(person, sentMessages[0])
    }

    @Test
    fun unsubscribeWorks() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic(NotificationTopic::class.java)

        val localClient = ClientBuilder.getNotificationClient(NotificationTopic::class.java)

        val id = localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.deleteSubscription(id)
        localClient.notify("{\"name\":\"Tom\", \"age\":15}")

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        assertEquals(0, sentMessages.size)
    }
}