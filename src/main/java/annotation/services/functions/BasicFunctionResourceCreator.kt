package annotation.services.functions

import annotation.annotations.function.BasicServerlessFunction
import annotation.annotations.function.repeatable.BasicServerlessFunctions
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import persisted.NimbusState
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import wrappers.basic.BasicServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class BasicFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        BasicServerlessFunction::class.java,
        BasicServerlessFunctions::class.java
) {



    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val basicFunctions = type.getAnnotationsByType(BasicServerlessFunction::class.java)
        val methodInformation = extractMethodInformation(type)

        val fileBuilder = BasicServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        fileBuilder.createClass()

        val handler = fileBuilder.getHandler()

        for (basicFunction in basicFunctions) {
            val cloudFormationDocuments = cfDocuments.getOrPut(basicFunction.stage) { CloudFormationDocuments() }
            val updateResources = cloudFormationDocuments.updateResources


            val config = FunctionConfig(basicFunction.timeout, basicFunction.memory, basicFunction.stage)
            val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

            //Configure cron if necessary
            if (basicFunction.cron != "") {
                functionEnvironmentService.newCronTrigger(basicFunction.cron, functionResource)
            }
            updateResources.addInvokableFunction(functionResource)


            results.add(FunctionInformation(type, functionResource))
        }
    }
}