package annotation.services.functions

import annotation.annotations.deployment.AfterDeployment
import annotation.annotations.deployment.AfterDeployments
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.deployment.DeploymentFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
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
            val cloudFormationDocuments = cfDocuments.getOrPut(afterDeployment.stage) { CloudFormationDocuments() }
            val updateResources = cloudFormationDocuments.updateResources

            val config = FunctionConfig(300, 1024, afterDeployment.stage)
            val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

            val afterDeploymentList = nimbusState.afterDeployments.getOrPut(afterDeployment.stage) { mutableListOf() }
            afterDeploymentList.add(functionResource.getFunctionName())

            updateResources.addResource(functionResource)

            results.add(FunctionInformation(type, functionResource))
        }
    }

}