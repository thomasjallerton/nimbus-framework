package localDeployment.unitTests

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import localDeployment.exampleHandlers.ExampleQueueHandler
import localDeployment.exampleModels.KeyValue
import localDeployment.exampleModels.Person
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QueueHandlerLocalTest {

    private val houseOne = KeyValue("testHouse", listOf(Person("TestPerson", 22)))

    @Test
    fun addingOneItemToQueueTriggersFunction() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize1")

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
        val queue = localDeployment.getQueue("BatchSize1")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handle")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)
        queue.add(houseOne)

        assertEquals(2, queueFunction.timesInvoked)
    }

    @Test
    fun addingOneItemToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize2Batch")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize2Batch")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Batched")
        assertEquals(0, queueFunction.timesInvoked)

        queue.addBatch(listOf(houseOne, houseOne))

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingOneItemToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize2Individual")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingTwoItemsToQueueTriggersIndividualFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize2Individual")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2Individual")
        assertEquals(0, queueFunction.timesInvoked)

        queue.addBatch(listOf(houseOne, houseOne))

        assertEquals(1, queueFunction.timesInvoked)
    }
}