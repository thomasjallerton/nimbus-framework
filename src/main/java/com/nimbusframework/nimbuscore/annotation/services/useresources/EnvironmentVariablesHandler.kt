package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.function.EnvironmentVariable
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class EnvironmentVariablesHandler(
        private val messager: Messager
) : UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        for (environmentVariable in serverlessMethod.getAnnotationsByType(EnvironmentVariable::class.java)) {
            for (stage in environmentVariable.stages) {
                if (stage == functionResource.stage) {
                    val envValue = handleEnvironmentVariable(environmentVariable.value)
                    functionResource.addEnvVariable(environmentVariable.key, envValue)
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
                messager.printMessage(Diagnostic.Kind.ERROR, "Function is configured to take environment variable $variableKey" +
                        " from local machine (as specified by the $value notation). This variable doesn't exist on machine.")
                value
            }
        } else {
            value
        }
    }

}