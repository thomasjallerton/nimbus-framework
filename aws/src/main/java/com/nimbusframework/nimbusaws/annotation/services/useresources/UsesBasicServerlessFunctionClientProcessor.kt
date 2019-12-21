package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesBasicServerlessFunctionAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesProcessor {

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