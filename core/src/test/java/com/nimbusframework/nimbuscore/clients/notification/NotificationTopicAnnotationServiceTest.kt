package com.nimbusframework.nimbuscore.clients.notification

import com.nimbusframework.nimbuscore.examples.notification.NotificationTopicNoStage
import com.nimbusframework.nimbuscore.examples.notification.NotificationTopic
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class NotificationTopicAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsTopic() {
        val topicName = NotificationTopicAnnotationService.getTopicName(NotificationTopic::class.java, "dev")
        topicName shouldBe "topic"
    }

    @Test
    fun correctlyGetsTopicWhenNoStageSet() {
        val topicName = NotificationTopicAnnotationService.getTopicName(NotificationTopicNoStage::class.java, "dev")
        topicName shouldBe "topic"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        NotificationTopicAnnotationService.getTopicName(NotificationTopic::class.java, "prod")
    }

}