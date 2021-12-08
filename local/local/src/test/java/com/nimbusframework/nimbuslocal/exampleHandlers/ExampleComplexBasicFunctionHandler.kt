package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction
import com.nimbusframework.nimbuscore.clients.ClientBuilder

open class ExampleComplexBasicFunctionHandler {

    @BasicServerlessFunction
    @UsesBasicServerlessFunction(targetClass = ExampleBasicFunctionHandler::class, methodName = "handle")
    open fun callOtherFunction(): Boolean {
        val function = ClientBuilder.getBasicServerlessFunctionInterface(ExampleBasicFunctionHandler::class.java)
        return function.handle("handle")
    }

}