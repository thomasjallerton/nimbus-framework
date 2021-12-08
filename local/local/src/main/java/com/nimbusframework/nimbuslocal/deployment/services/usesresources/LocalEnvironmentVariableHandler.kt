package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.function.EnvironmentVariable
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalEnvironmentVariableHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val environmentVariables = method.getAnnotationsByType(EnvironmentVariable::class.java)

        val annotation = stageService.annotationForStage(environmentVariables) { annotation -> annotation.stages}
        if (annotation != null) {
            if (annotation.testValue == "NIMBUS_NOT_SET") {
                val envValue = handleEnvironmentVariable(annotation.value)
                functionEnvironment.addEnvironmentVariable(annotation.key, envValue)
            } else {
                functionEnvironment.addEnvironmentVariable(annotation.key, annotation.testValue)
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