package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.function.EnvironmentVariable
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalEnvironmentVariableHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val environmentVariables = method.getAnnotationsByType(EnvironmentVariable::class.java)

        for (environmentVariable in environmentVariables) {
            if (environmentVariable.stages.contains(stage)) {
                if (environmentVariable.testValue == "NIMBUS_NOT_SET") {
                    val envValue = handleEnvironmentVariable(environmentVariable.value)
                    functionEnvironment.addEnvironmentVariable(environmentVariable.key, envValue)
                } else {
                    functionEnvironment.addEnvironmentVariable(environmentVariable.key, environmentVariable.testValue)
                }
            }
        }
    }

    private fun handleEnvironmentVariable(value: String): String {
        return if (value.startsWith("\${") && value.endsWith("}")) {
            val variableKey = value.substringAfter("\${").dropLast(1)
            if (System.getenv().containsKey(variableKey)) {
                System.getenv(variableKey)
            } else {
                throw Exception("Function is configured to take environment variable $variableKey" +
                        " from local machine (as specified by the $value notation). This variable doesn't exist on machine.")
            }
        } else {
            value
        }
    }

}