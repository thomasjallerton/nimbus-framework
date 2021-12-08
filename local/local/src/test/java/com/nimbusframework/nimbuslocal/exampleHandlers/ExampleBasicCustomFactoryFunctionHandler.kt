package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactory
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction

@CustomFactory(BasicFactory::class)
open class ExampleBasicCustomFactoryFunctionHandler(private val result: Boolean) {
    constructor(): this(true)

    @BasicServerlessFunction
    open fun handle(): Boolean {
        return result
    }
}