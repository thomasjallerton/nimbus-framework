package com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesBasicServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientProcessor(
    private val cfDocuments: Map<String, CloudFormationFiles>,
    private val processingEnv: ProcessingEnvironment,
    nimbusState: NimbusState,
    private val messager: Messager
): UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {

        for (usesBasicServerlessFunctionClient in serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunction::class.java)) {

            val targetElem = UsesBasicServerlessFunctionAnnotation(usesBasicServerlessFunctionClient).getTypeElement(processingEnv)

            for (stage in stageService.determineStages(usesBasicServerlessFunctionClient.stages)) {
                if (stage == functionResource.stage) {

                    functionResource.addEnvVariable(ConstantEnvironmentVariable.NIMBUS_PROJECT_NAME, nimbusState.projectName)
                    functionResource.addEnvVariable(ConstantEnvironmentVariable.FUNCTION_STAGE, stage)
                    val cfDocument = cfDocuments.getValue(stage)
                    val updateResources = cfDocument.updateTemplate.resources
                    val function = updateResources.getInvokableFunction(
                            targetElem.simpleName.toString(),
                            usesBasicServerlessFunctionClient.methodName)
                    if (function != null) {
                        functionResource.getIamRoleResource().addAllowStatement("lambda:*", function, "")
                    } else {
                        messager.printMessage(
                                Diagnostic.Kind.ERROR,
                                "${targetElem.simpleName} does not contain a BasicServerlessFunction ${usesBasicServerlessFunctionClient.methodName}",
                                serverlessMethod
                        )
                    }
                }
            }
        }
    }
}
