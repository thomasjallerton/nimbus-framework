package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.deployment.DeploymentFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployments
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class AfterDeploymentResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        private val processingEnv: ProcessingEnvironment
): FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        AfterDeployment::class.java,
        AfterDeployments::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val afterDeployments = type.getAnnotationsByType(AfterDeployment::class.java)

        val methodInformation = extractMethodInformation(type)
        val fileBuilder = DeploymentFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        fileBuilder.createClass()

        val handler = fileBuilder.getHandler()

        for (afterDeployment in afterDeployments) {
            val stages = stageService.determineStages(afterDeployment.stages)

            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = stages
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val config = FunctionConfig(300, 1024, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        handler,
                        methodInformation,
                        handlerInformation,
                        config
                )

                val afterDeploymentList = nimbusState.afterDeployments.getOrPut(stage) { mutableListOf() }
                afterDeploymentList.add(functionResource.getFunctionName())

                updateResources.addResource(functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }

}