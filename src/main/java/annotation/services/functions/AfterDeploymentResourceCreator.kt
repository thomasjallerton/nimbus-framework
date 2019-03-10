package annotation.services.functions

import annotation.annotations.deployment.AfterDeployment
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.deployment.DeploymentFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class AfterDeploymentResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        private val nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(cfDocuments, nimbusState, processingEnv) {

    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(AfterDeployment::class.java)
        val results = LinkedList<FunctionInformation>()
        for (type in annotatedElements) {
            val afterDeployment = type.getAnnotation(AfterDeployment::class.java)

            val methodInformation = extractMethodInformation(type)

            val cloudFormationDocuments = cfDocuments.getOrPut(afterDeployment.stage) {CloudFormationDocuments()}
            val updateResources = cloudFormationDocuments.updateResources

            val fileBuilder = DeploymentFunctionFileBuilder(
                    processingEnv,
                    methodInformation,
                    type
            )

            val handler = fileBuilder.getHandler()

            val config = FunctionConfig(300, 1024, afterDeployment.stage)
            val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

            val afterDeploymentList = nimbusState.afterDeployments.getOrPut(afterDeployment.stage) { mutableListOf()}
            afterDeploymentList.add(functionResource.getFunctionName())

            updateResources.addResource(functionResource)

            fileBuilder.createClass()

            results.add(FunctionInformation(type, functionResource))
        }
        return results
    }

}