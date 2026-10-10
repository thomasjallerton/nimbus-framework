package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.notification.Protocol
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleNotificationHandler
import com.nimbusframework.nimbuslocal.exampleModels.NotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class NotificationClientLocalTest: AnnotationSpec() {

    @Test
    fun subscribingWorksAndSendingMessageWorks() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)
        val topic = localDeployment.getNotificationTopic(NotificationTopic::class.java)

        val localClient = ClientBuilder.getNotificationClient(NotificationTopic::class.java)

        localClient.createSubscription(Protocol.HTTP, "www.test.com")
        localClient.notify("{\"name\":\"Tom\", \"age\":15}")

        val sentMessages = topic.getEndpointsMessages(Protocol.HTTP, "www.test.com")

        sentMessages.size shouldBe 1
        sentMessages[0] shouldBe "{\"name\":\"Tom\", \"age\":15}"
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

        sentMessages.size shouldBe 1
        sentMessages[0] shouldBe person
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

        sentMessages.size shouldBe 0
    }
}