package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.ResourceNotFoundException
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleHttpHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class HttpHandlerLocalTest: AnnotationSpec() {

    private val testPerson = Person("thomas", 21)

    @Test
    fun onlyGetTriggersGetMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"getRequest")

        val httpRequest = HttpRequest("test", HttpMethod.GET)

        httpMethod.timesInvoked shouldBe 0

        localDeployment.sendHttpRequest(httpRequest)

        httpMethod.timesInvoked shouldBe 1
        httpMethod.mostRecentValueReturned shouldBe true

        val wrongHttpRequest = HttpRequest("test", HttpMethod.POST)

        shouldThrow<ResourceNotFoundException> {
            localDeployment.sendHttpRequest(wrongHttpRequest)
        }
    }

    @Test
    fun anythingTriggersAnyMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"anyRequest")

        val httpRequest = HttpRequest("any", HttpMethod.GET)

        httpMethod.timesInvoked shouldBe 0

        localDeployment.sendHttpRequest(httpRequest)

        httpMethod.timesInvoked shouldBe 1
        httpMethod.mostRecentValueReturned shouldBe true

        val anotherHttpRequest = HttpRequest("any", HttpMethod.POST)

        localDeployment.sendHttpRequest(anotherHttpRequest)

        httpMethod.timesInvoked shouldBe 2
        httpMethod.mostRecentValueReturned shouldBe true
    }

    @Test
    fun postTriggersPostMethod() {

        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val httpMethod = localDeployment.getMethod(ExampleHttpHandler::class.java,"postRequest")

        val httpRequest = HttpRequest("newPerson", HttpMethod.POST)
        httpRequest.setBodyFromObject(testPerson)

        httpMethod.timesInvoked shouldBe 0

        localDeployment.sendHttpRequest(httpRequest)

        httpMethod.timesInvoked shouldBe 1
        httpMethod.mostRecentValueReturned shouldBe true
        httpMethod.mostRecentInvokeArgument shouldBe testPerson
    }
}
