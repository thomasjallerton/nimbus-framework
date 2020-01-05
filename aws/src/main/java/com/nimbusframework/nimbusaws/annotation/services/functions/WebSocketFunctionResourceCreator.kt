package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.WebSocketServerlessFunctions
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.websocket.WebSocketServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        private val processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
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
            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = webSocketFunction.stages.toSet()
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in webSocketFunction.stages) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(webSocketFunction.timeout, webSocketFunction.memory, stage)

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