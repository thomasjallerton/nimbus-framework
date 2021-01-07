package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.websocket.WebSocketServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.WebSocketServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment,
        messager: Messager
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        messager,
        WebSocketServerlessFunction::class.java,
        WebSocketServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val webSocketFunctions = type.getAnnotationsByType(WebSocketServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)


        val fileBuilder = WebSocketServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        fileBuilder.createClass()

        for (webSocketFunction in webSocketFunctions) {
            val stages = stageService.determineStages(webSocketFunction.stages)

            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = stages
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
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
        return results
    }
}