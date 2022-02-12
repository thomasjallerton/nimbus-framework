package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.deployment.DeploymentFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployments
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
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

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation(), false))
            }
        }
        return results
    }

}
