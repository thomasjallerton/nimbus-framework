package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleBasicCustomFactoryFunctionHandler
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleBasicFunctionHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class BasicFunctionHandlerTest: AnnotationSpec() {

    @Test
    fun triggerMethodActuallyTriggersMethod() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)

        val method = localDeployment.getBasicFunction(ExampleBasicFunctionHandler::class.java, "handle")

        val result = method.invoke("HELLO", Boolean::class.java)

        result shouldBe true
        method.mostRecentValueReturned shouldBe true

        method.mostRecentInvokeArgument shouldBe "HELLO"
        method.timesInvoked shouldBe 1
    }

    @Test
    fun testClientActuallyTriggersMethod() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)

        val method = localDeployment.getBasicFunction(ExampleBasicFunctionHandler::class.java, "handle")

        val client = ClientBuilder.getBasicServerlessFunctionClient(ExampleBasicFunctionHandler::class.java, "handle")

        val result = client.invoke("HELLO", Boolean::class.java)

        result shouldBe true
        method.mostRecentValueReturned shouldBe true

        method.mostRecentInvokeArgument shouldBe "HELLO"
        method.timesInvoked shouldBe 1
    }

    @Test
    fun canGetInstanceWithFactory() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicCustomFactoryFunctionHandler::class.java)
        val client = ClientBuilder.getBasicServerlessFunctionInterface(ExampleBasicCustomFactoryFunctionHandler::class.java)
        client.handle() shouldBe true
    }
}
