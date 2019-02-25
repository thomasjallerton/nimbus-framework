package localDeployment.exampleHandlers

import annotation.annotations.function.QueueServerlessFunction
import localDeployment.exampleModels.KeyValue
import wrappers.queue.models.QueueEvent

class ExampleQueueHandler {

    @QueueServerlessFunction(batchSize = 1, id = "BatchSize1")
    fun handle(house: KeyValue, queueEvent: QueueEvent): Boolean {
        return true
    }

    @QueueServerlessFunction(batchSize = 2, id = "BatchSize2")
    fun handleBatchSize2(house: List<KeyValue>, queueEvent: List<QueueEvent>): Boolean {
        return true
    }
}