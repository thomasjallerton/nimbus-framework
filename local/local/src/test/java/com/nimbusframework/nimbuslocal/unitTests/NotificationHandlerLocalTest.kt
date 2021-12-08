package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleNotificationHandler
import com.nimbusframework.nimbuslocal.exampleModels.NotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NotificationHandlerLocalTest: AnnotationSpec() {

    private val testPerson = Person("Thomas", 21)

    @Test
    fun testSendingNotificationTriggersFunction() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)

        val notificationFunction = localDeployment.getMethod(ExampleNotificationHandler::class.java, "receiveNotification")

        assertEquals(0, notificationFunction.timesInvoked)

        val testTopic = localDeployment.getNotificationTopic(NotificationTopic::class.java)
        testTopic.notifyJson(testPerson)

        assertEquals(1, notificationFunction.timesInvoked)
        assertEquals(testPerson, notificationFunction.mostRecentInvokeArgument)
        assertEquals(testPerson, notificationFunction.mostRecentValueReturned)
    }
}