package localDeployment.exampleHandlers

import annotation.annotations.function.BasicServerlessFunction

class ExampleBasicFunctionHandler {

    @BasicServerlessFunction
    fun handle(input: String): Boolean {
        return true
    }
}