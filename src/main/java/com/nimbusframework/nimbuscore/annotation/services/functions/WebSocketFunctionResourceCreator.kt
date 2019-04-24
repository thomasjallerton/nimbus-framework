package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.WebSocketServerlessFunctions
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.websocket.WebSocketServerlessFunctionFileBuilder
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
                type,
                nimbusState
        )

        fileBuilder.createClass()

        for (webSocketFunction in webSocketFunctions) {
            for (stage in webSocketFunction.stages) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(webSocketFunction.timeout, webSocketFunction.memory, stage)
                val handlerInformation = HandlerInformation(handlerClassPath = fileBuilder.classFilePath(), handlerFile = fileBuilder.handlerFile())

                val functionResource = functionEnvironmentService.newFunction(
                        handler,
                        methodInformation,
                        handlerInformation,
                        config
                )

                functionEnvironmentService.newWebSocketRoute(webSocketFunction.topic, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}