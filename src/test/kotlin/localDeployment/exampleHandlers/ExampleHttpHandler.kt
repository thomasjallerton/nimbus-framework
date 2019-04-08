package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod
import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpServerlessFunction
import localDeployment.exampleModels.Person

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