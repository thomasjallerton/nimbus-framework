package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployments
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.deployment.DeploymentFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class AfterDeploymentResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        AfterDeployment::class.java,
        AfterDeployments::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val afterDeployments = type.getAnnotationsByType(AfterDeployment::class.java)

        val methodInformation = extractMethodInformation(type)
        val fileBuilder = DeploymentFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        fileBuilder.createClass()

        val handler = fileBuilder.getHandler()

        for (afterDeployment in afterDeployments) {
            for (stage in afterDeployment.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val updateResources = cloudFormationDocuments.updateResources

                val config = FunctionConfig(300, 1024, stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                val afterDeploymentList = nimbusState.afterDeployments.getOrPut(stage) { mutableListOf() }
                afterDeploymentList.add(functionResource.getFunctionName())

                updateResources.addResource(functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }

}