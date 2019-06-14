package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.function.UsesBasicServerlessFunction
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.UsesBasicServerlessFunctionAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientHandler(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {

        for (usesBasicServerlessFunctionClient in serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunction::class.java)) {
            functionResource.addClient(ClientType.BasicFunction)

            val targetElem = UsesBasicServerlessFunctionAnnotation(usesBasicServerlessFunctionClient).getTypeElement(processingEnv)

            for (stage in usesBasicServerlessFunctionClient.stages) {
                if (stage == functionResource.stage) {

                    functionResource.addEnvVariable("NIMBUS_PROJECT_NAME", nimbusState.projectName)
                    functionResource.addEnvVariable("FUNCTION_STAGE", stage)
                    val cfDocument = cfDocuments.getValue(stage)
                    val updateResources = cfDocument.updateTemplate.resources
                    val function = updateResources.getInvokableFunction(
                            targetElem.simpleName.toString(),
                            usesBasicServerlessFunctionClient.methodName)
                    if (function != null) {
                        functionResource.getIamRoleResource().addAllowStatement("lambda:*", function, "")
                    } else {
                        val messager = processingEnv.messager
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