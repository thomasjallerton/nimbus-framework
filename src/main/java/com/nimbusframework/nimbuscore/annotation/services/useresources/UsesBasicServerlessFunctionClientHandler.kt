package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.function.UsesBasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.DataModelAnnotation
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.UsesBasicServerlessFunctionAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {

        for (usesBasicServerlessFunctionClient in serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunctionClient::class.java)) {
            functionResource.addClient(ClientType.BasicFunction)

            val targetElem = UsesBasicServerlessFunctionAnnotation(usesBasicServerlessFunctionClient).getTypeElement(processingEnv)

            for (stage in usesBasicServerlessFunctionClient.stages) {
                if (stage == functionResource.stage) {

                    functionResource.addEnvVariable("NIMBUS_PROJECT_NAME", nimbusState.projectName)
                    functionResource.addEnvVariable("FUNCTION_STAGE", stage)
                    val cfDocument = cfDocuments.getValue(stage)
                    val updateResources = cfDocument.updateResources
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