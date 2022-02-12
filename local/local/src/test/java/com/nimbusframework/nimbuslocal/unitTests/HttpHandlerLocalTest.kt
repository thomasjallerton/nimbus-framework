package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.ResourceNotFoundException
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleHttpHandler
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HttpHandlerLocalTest: AnnotationSpec() {

    private val testPerson = Person("thomas", 21)

    @Test
    fun onlyGetTriggersGetMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"getRequest")

        val httpRequest = HttpRequest("test", HttpMethod.GET)

        assertEquals(0, httpMethod.timesInvoked)

        localDeployment.sendHttpRequest(httpRequest)

        assertEquals(1, httpMethod.timesInvoked)
        assertEquals(true, httpMethod.mostRecentValueReturned)

        val wrongHttpRequest = HttpRequest("test", HttpMethod.POST)

        try {
            localDeployment.sendHttpRequest(wrongHttpRequest)
        } catch (e: ResourceNotFoundException) {
            return
        }
        assertFalse(true)
    }

    @Test
    fun anythingTriggersAnyMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"anyRequest")

        val httpRequest = HttpRequest("any", HttpMethod.GET)

        assertEquals(0, httpMethod.timesInvoked)

        localDeployment.sendHttpRequest(httpRequest)

        assertEquals(1, httpMethod.timesInvoked)
        assertEquals(true, httpMethod.mostRecentValueReturned)

        val anotherHttpRequest = HttpRequest("any", HttpMethod.POST)

        localDeployment.sendHttpRequest(anotherHttpRequest)

        assertEquals(2, httpMethod.timesInvoked)
        assertEquals(true, httpMethod.mostRecentValueReturned)
    }

    @Test
    fun postTriggersPostMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"postRequest")

        val httpRequest = HttpRequest("newPerson", HttpMethod.POST)
        httpRequest.setBodyFromObject(testPerson)

        assertEquals(0, httpMethod.timesInvoked)

        localDeployment.sendHttpRequest(httpRequest)

        assertEquals(1, httpMethod.timesInvoked)
        assertEquals(true, httpMethod.mostRecentValueReturned)
        assertEquals(testPerson, httpMethod.mostRecentInvokeArgument)
    }
}