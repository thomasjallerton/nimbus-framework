package annotation.services.functions

import annotation.annotations.function.WebSocketServerlessFunction
import annotation.annotations.function.repeatable.WebSocketServerlessFunctions
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.websocket.WebSocketServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        WebSocketServerlessFunction::class.java,
        WebSocketServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val webSocketFunctions = type.getAnnotationsByType(WebSocketServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)


        val fileBuilder = WebSocketServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        fileBuilder.createClass()

        for (webSocketFunction in webSocketFunctions) {
            for (stage in webSocketFunction.stages) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(webSocketFunction.timeout, webSocketFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                functionEnvironmentService.newWebSocketRoute(webSocketFunction.topic, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}