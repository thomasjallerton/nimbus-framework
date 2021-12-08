package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleQueueHandler
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QueueHandlerLocalTest: AnnotationSpec() {

    private val houseOne = KeyValue("testHouse", listOf(Person("TestPerson", 22)))

    @Test
    fun addingOneItemToQueueTriggersFunction() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize1Queue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handle")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
        assertEquals(houseOne, queueFunction.mostRecentInvokeArgument)
        assertEquals(true, queueFunction.mostRecentValueReturned)
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionTwice() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize1Queue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handle")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)
        queue.add(houseOne)

        assertEquals(2, queueFunction.timesInvoked)
    }

    @Test
    fun addingOneItemToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2BatchQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2BatchQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        assertEquals(0, queueFunction.timesInvoked)

        queue.addBatch(listOf(houseOne, houseOne))

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingOneItemToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2IndividualQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingTwoItemsToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue(ExampleQueueHandler.BatchSize2IndividualQueue::class.java)

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        assertEquals(0, queueFunction.timesInvoked)

        queue.addBatch(listOf(houseOne, houseOne))

        assertEquals(1, queueFunction.timesInvoked)
    }
}