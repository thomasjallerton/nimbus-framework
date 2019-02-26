package localDeployment

import clients.ClientBuilder
import localDeployment.exampleHandlers.ExampleBasicFunctionHandler
import org.junit.jupiter.api.Test
import testing.LocalNimbusDeployment
import kotlin.test.assertEquals

class BasicFunctionHandlerTest {

    @Test
    fun triggerMethodActuallyTriggersMethod() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)

        val method = localDeployment.getBasicMethod(ExampleBasicFunctionHandler::class.java, "handle")

        val result = method.invoke("HELLO", Boolean::class.java)

        assertEquals(true, result)
        assertEquals(true, method.mostRecentValueReturned)

        assertEquals("HELLO", method.mostRecentInvokeArgument)
        assertEquals(1, method.timesInvoked)
    }

    @Test
    fun testClientActuallyTriggersMethod() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)

        val method = localDeployment.getBasicMethod(ExampleBasicFunctionHandler::class.java, "handle")

        val client = ClientBuilder.getBasicServerlessFunctionClient()

        val result = client.invoke(ExampleBasicFunctionHandler::class.java, "handle", "HELLO", Boolean::class.java)

        assertEquals(true, result)
        assertEquals(true, method.mostRecentValueReturned)

        assertEquals("HELLO", method.mostRecentInvokeArgument)
        assertEquals(1, method.timesInvoked)
    }
}