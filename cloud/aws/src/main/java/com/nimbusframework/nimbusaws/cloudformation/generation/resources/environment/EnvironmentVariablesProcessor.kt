package com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.function.EnvironmentVariable
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class EnvironmentVariablesProcessor(
        nimbusState: NimbusState,
        private val messager: Messager
) : UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        for (rawEnvironmentVariable in serverlessMethod.getAnnotationsByType(EnvironmentVariable::class.java)) {
            val environmentVariable = ParsedEnvironmentVariable(rawEnvironmentVariable)

            for (stage in stageService.determineStages(environmentVariable.stages)) {
                if (stage == functionResource.stage) {
                    val envValue = handleEnvironmentVariable(environmentVariable.value)
                    functionResource.addEnvVariable(CustomEnvironmentVariable(environmentVariable), envValue)
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
