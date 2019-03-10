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
) : FunctionResourceCreator(cfDocuments, nimbusState, processingEnv) {

    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(HttpServerlessFunction::class.java)
        val annotatedElementsRepeatable = roundEnv.getElementsAnnotatedWith(HttpServerlessFunctions::class.java)

        val results = LinkedList<FunctionInformation>()

        annotatedElements.forEach { type -> handleElement(type, functionEnvironmentService, results) }
        annotatedElementsRepeatable.forEach { type -> handleElement(type, functionEnvironmentService, results) }

        return results
    }

    private fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val httpFunctions = type.getAnnotationsByType(HttpServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)


        val fileBuilder = HttpServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        fileBuilder.createClass()

        for (httpFunction in httpFunctions) {
            if (type.kind == ElementKind.METHOD) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(httpFunction.timeout, httpFunction.memory, httpFunction.stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}