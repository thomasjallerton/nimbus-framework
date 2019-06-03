package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.wrappers.queue.models.QueueEvent
import localDeployment.exampleModels.KeyValue

class ExampleQueueHandler {

    @QueueServerlessFunction(batchSize = 1, id = "BatchSize1")
    fun handle(house: KeyValue, queueEvent: QueueEvent): Boolean {
        return true
    }

    @QueueServerlessFunction(batchSize = 2, id = "BatchSize2Batch")
    fun handleBatchSize2Batched(house: List<KeyValue>, queueEvent: List<QueueEvent>): Boolean {
        if (queueEvent.isNotEmpty()) {
            val firstId = queueEvent.first().requestId
            queueEvent.forEach {
                assert(it.requestId == firstId)
            }
        }
        return true
    }

    @QueueServerlessFunction(batchSize = 2, id = "BatchSize2Individual")
    fun handleBatchSize2Individual(house: KeyValue, queueEvent: QueueEvent): Boolean {
        return true
    }
}