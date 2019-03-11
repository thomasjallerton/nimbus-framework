package annotation.services.functions

import annotation.annotations.function.HttpServerlessFunction
import annotation.annotations.function.repeatable.HttpServerlessFunctions
import persisted.NimbusState
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import wrappers.http.HttpServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class HttpFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        HttpServerlessFunction::class.java,
        HttpServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val httpFunctions = type.getAnnotationsByType(HttpServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)


        val fileBuilder = HttpServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        fileBuilder.createClass()

        for (httpFunction in httpFunctions) {
            for (stage in httpFunction.stages) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(httpFunction.timeout, httpFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}