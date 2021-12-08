package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuslocal.exampleModels.Person

class ExampleHttpHandler {

    @HttpServerlessFunction(path = "test", method = HttpMethod.GET)
    fun getRequest(): Boolean {
        return true
    }

    @HttpServerlessFunction(path = "newPerson", method = HttpMethod.POST)
    fun postRequest(person: Person): Boolean {
        return true
    }


    @HttpServerlessFunction(path = "any", method = HttpMethod.ANY)
    fun anyRequest(): Boolean {
        return true
    }
}