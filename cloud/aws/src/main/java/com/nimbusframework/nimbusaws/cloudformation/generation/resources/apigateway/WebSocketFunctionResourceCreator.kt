package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.websocket.WebSocketServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.WebSocketServerlessFunctions
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class WebSocketFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
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
            classForReflectionService
        )

        fileBuilder.createClass()

        for (webSocketFunction in webSocketFunctions) {
            val stages = stageService.determineStages(webSocketFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)

            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {

                val config = FunctionConfig(webSocketFunction.timeout, webSocketFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                functionEnvironmentService.newWebSocketRoute(webSocketFunction.topic, functionResource)

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }
}
