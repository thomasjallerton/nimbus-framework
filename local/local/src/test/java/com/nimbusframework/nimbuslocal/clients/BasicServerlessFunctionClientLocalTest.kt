package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleBasicFunctionHandler
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleComplexBasicFunctionHandler
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BasicServerlessFunctionClientLocalTest: StringSpec({

    "can call basic serverless function" {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)
        val basicFunction = localDeployment.getBasicFunction(ExampleBasicFunctionHandler::class.java, "handle")
        val result = basicFunction.invoke("input", Boolean::class.java)
        result shouldBe true
    }

    "can call basic serverless function interface" {
        LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java)
        val functionHandler = ClientBuilder.getBasicServerlessFunctionInterface(ExampleBasicFunctionHandler::class.java)
        val result = functionHandler.handle("input")
        result shouldBe true
    }

    "can call chained serverless function" {
        val localDeployment = LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java, ExampleComplexBasicFunctionHandler::class.java)
        val basicFunction = localDeployment.getBasicFunction(ExampleComplexBasicFunctionHandler::class.java, "callOtherFunction")
        val result = basicFunction.invoke(Boolean::class.java)
        result shouldBe true
    }

    "can call chained serverless function interface" {
        LocalNimbusDeployment.getNewInstance(ExampleBasicFunctionHandler::class.java, ExampleComplexBasicFunctionHandler::class.java)
        val functionHandler = ClientBuilder.getBasicServerlessFunctionInterface(ExampleComplexBasicFunctionHandler::class.java)
        val result = functionHandler.callOtherFunction()
        result shouldBe true
    }

})