package com.nimbusframework.nimbuscore.clients.queue

import com.nimbusframework.nimbuscore.examples.queue.Queue
import com.nimbusframework.nimbuscore.examples.queue.QueueNoStage
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec

internal class QueueIdAnnotationServiceTest : AnnotationSpec() {

    @Test
    fun correctlyGetsQueueId() {
        val queueId = QueueIdAnnotationService.getQueueId(Queue::class.java, "dev")
        queueId shouldBe "queueId"
    }

    @Test
    fun correctlyGetsQueueIdWhenNoStageSet() {
        val queueId = QueueIdAnnotationService.getQueueId(QueueNoStage::class.java, "dev")
        queueId shouldBe "queueId"
    }

    @Test(expected = InvalidStageException::class)
    fun throwsExceptionWhenWrongStage() {
        QueueIdAnnotationService.getQueueId(Queue::class.java, "prod")
    }

}