package com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.deployment.DeploymentFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployments
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class AfterDeploymentResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
    messager,
    AfterDeployment::class.java,
    AfterDeployments::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val afterDeployments = type.getAnnotationsByType(AfterDeployment::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)
        val fileBuilder = DeploymentFunctionFileBuilder(
            processingEnv,
            methodInformation,
            type,
            classForReflectionService
        )

        fileBuilder.createClass()

        for (afterDeployment in afterDeployments) {
            val stages = stageService.determineStages(afterDeployment.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val config = FunctionConfig(300, 1024, stage)

                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                val afterDeploymentList = nimbusState.afterDeployments.getOrPut(stage) { mutableListOf() }
                afterDeploymentList.add(functionResource.getFunctionName())

                updateResources.addFunction(functionResource)

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation(), false))
            }
        }
        return results
    }

}
