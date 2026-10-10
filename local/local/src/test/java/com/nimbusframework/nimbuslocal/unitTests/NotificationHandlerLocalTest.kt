package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleNotificationHandler
import com.nimbusframework.nimbuslocal.exampleModels.NotificationTopic
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class NotificationHandlerLocalTest: AnnotationSpec() {

    private val testPerson = Person("Thomas", 21)

    @Test
    fun testSendingNotificationTriggersFunction() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(NotificationTopic::class.java, ExampleNotificationHandler::class.java)

        val notificationFunction = localDeployment.getMethod(ExampleNotificationHandler::class.java, "receiveNotification")

        notificationFunction.timesInvoked shouldBe 0

        val testTopic = localDeployment.getNotificationTopic(NotificationTopic::class.java)
        testTopic.notifyJson(testPerson)

        notificationFunction.timesInvoked shouldBe 1
        notificationFunction.mostRecentInvokeArgument shouldBe testPerson
        notificationFunction.mostRecentValueReturned shouldBe testPerson
    }
}
