package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleQueueHandler
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class QueueHandlerLocalTest: AnnotationSpec() {

    private val houseOne = KeyValue("testHouse", listOf(Person("TestPerson", 22)))

    @Test
    fun addingOneItemToQueueTriggersFunction() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize1Queue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handle")
        queueFunction.timesInvoked shouldBe 0

        queue.add(houseOne)

        queueFunction.timesInvoked shouldBe 1
        queueFunction.mostRecentInvokeArgument shouldBe houseOne
        queueFunction.mostRecentValueReturned shouldBe true
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionTwice() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize1Queue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handle")
        queueFunction.timesInvoked shouldBe 0

        queue.add(houseOne)
        queue.add(houseOne)

        queueFunction.timesInvoked shouldBe 2
    }

    @Test
    fun addingOneItemToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2BatchQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        queueFunction.timesInvoked shouldBe 0

        queue.add(houseOne)

        queueFunction.timesInvoked shouldBe 1
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2BatchQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        queueFunction.timesInvoked shouldBe 0

        queue.addBatch(listOf(houseOne, houseOne))

        queueFunction.timesInvoked shouldBe 1
    }

    @Test
    fun addingOneItemToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2IndividualQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        queueFunction.timesInvoked shouldBe 0

        queue.add(houseOne)

        queueFunction.timesInvoked shouldBe 1
    }

    @Test
    fun addingTwoItemsToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java, ExampleQueueHandler.BatchSize1Queue::class.java,
            ExampleQueueHandler.BatchSize2BatchQueue::class.java, ExampleQueueHandler.BatchSize2IndividualQueue::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2IndividualQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        queueFunction.timesInvoked shouldBe 0

        queue.addBatch(listOf(houseOne, houseOne))

        queueFunction.timesInvoked shouldBe 1
    }
}
