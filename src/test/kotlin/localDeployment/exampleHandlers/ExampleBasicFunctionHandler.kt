package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.function.BasicServerlessFunction

class ExampleBasicFunctionHandler {

    @BasicServerlessFunction
    fun handle(input: String): Boolean {
        return true
    }
}