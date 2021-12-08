package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue

class ExampleQueueHandler {

    @QueueServerlessFunction(queue = BatchSize1Queue::class, batchSize = 1)
    fun handle(house: KeyValue, queueEvent: QueueEvent): Boolean {
        return true
    }

    @QueueServerlessFunction(queue = BatchSize2BatchQueue::class, batchSize = 2)
    fun handleBatchSize2Batched(house: List<KeyValue>, queueEvent: List<QueueEvent>): Boolean {
        if (queueEvent.isNotEmpty()) {
            val firstId = queueEvent.first().requestId
            queueEvent.forEach {
                assert(it.requestId == firstId)
            }
        }
        return true
    }

    @QueueServerlessFunction(queue = BatchSize2IndividualQueue::class, batchSize = 2)
    fun handleBatchSize2Individual(house: KeyValue, queueEvent: QueueEvent): Boolean {
        return true
    }

    @QueueDefinition(queueId = "BatchSize1")
    class BatchSize1Queue

    @QueueDefinition(queueId = "BatchSize2Batch")
    class BatchSize2BatchQueue

    @QueueDefinition(queueId = "BatchSize2Individual")
    class BatchSize2IndividualQueue
}