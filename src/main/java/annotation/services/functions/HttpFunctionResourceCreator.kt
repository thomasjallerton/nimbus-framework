package annotation.services.functions

import annotation.annotations.function.HttpServerlessFunction
import cloudformation.persisted.NimbusState
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import wrappers.http.HttpServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind

class HttpFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(updateResources, nimbusState, processingEnv) {

    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(HttpServerlessFunction::class.java)

        val results = LinkedList<FunctionInformation>()

        for (type in annotatedElements) {
            val httpFunction = type.getAnnotation(HttpServerlessFunction::class.java)

            if (type.kind == ElementKind.METHOD) {
                val methodInformation = extractMethodInformation(type)


                val fileBuilder = HttpServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                )

                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(httpFunction.timeout, httpFunction.memory)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results
    }

}