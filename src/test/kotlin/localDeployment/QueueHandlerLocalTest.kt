package localDeployment

import localDeployment.exampleHandlers.ExampleQueueHandler
import org.junit.jupiter.api.Test
import testing.LocalNimbusDeployment
import testpackage.House
import testpackage.Person
import kotlin.test.assertEquals

class QueueHandlerLocalTest {

    private val houseOne = House("testHouse", listOf(Person("TestPerson", 22)))

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
        val queue = localDeployment.getQueue("BatchSize2")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2")
        assertEquals(0, queueFunction.timesInvoked)

        queue.add(houseOne)

        assertEquals(1, queueFunction.timesInvoked)
    }

    @Test
    fun addingTwoItemsToQueueTriggersFunctionOnceLargerBatchsize() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleQueueHandler::class.java)
        val queue = localDeployment.getQueue("BatchSize2")

        val queueFunction = localDeployment.getMethod(ExampleQueueHandler::class.java, "handleBatchSize2")
        assertEquals(0, queueFunction.timesInvoked)

        queue.addBatch(listOf(houseOne, houseOne))

        assertEquals(1, queueFunction.timesInvoked)
    }
}