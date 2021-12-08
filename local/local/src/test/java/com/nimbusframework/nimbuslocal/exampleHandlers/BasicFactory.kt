package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactoryInterface

class BasicFactory: CustomFactoryInterface<ExampleBasicCustomFactoryFunctionHandler> {

    override fun create(): ExampleBasicCustomFactoryFunctionHandler {
        return ExampleBasicCustomFactoryFunctionHandler(true)
    }

}