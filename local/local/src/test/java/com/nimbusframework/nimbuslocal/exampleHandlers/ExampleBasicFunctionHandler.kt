package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction

open class ExampleBasicFunctionHandler {

    @BasicServerlessFunction
    open fun handle(input: String): Boolean {
        return true
    }
}