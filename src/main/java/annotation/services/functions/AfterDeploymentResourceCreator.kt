package annotation.services.functions

import annotation.annotations.deployment.AfterDeployment
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.deployment.DeploymentFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class AfterDeploymentResourceCreator(
        private val updateResources: ResourceCollection,
        private val nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(updateResources, nimbusState, processingEnv) {

    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(AfterDeployment::class.java)
        val results = LinkedList<FunctionInformation>()
        for (type in annotatedElements) {
            val methodInformation = extractMethodInformation(type)

            val fileBuilder = DeploymentFunctionFileBuilder(
                    processingEnv,
                    methodInformation,
                    type
            )

            val handler = fileBuilder.getHandler()

            val config = FunctionConfig(300, 1024)
            val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

            nimbusState.afterDeployments.add(functionResource.getFunctionName())
            updateResources.addResource(functionResource)

            fileBuilder.createClass()

            results.add(FunctionInformation(type, functionResource))
        }
        return results
    }

}