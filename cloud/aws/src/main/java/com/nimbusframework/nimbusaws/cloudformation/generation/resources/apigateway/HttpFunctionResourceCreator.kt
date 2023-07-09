package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.http.HttpServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class HttpFunctionResourceCreator(
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
    HttpServerlessFunction::class.java,
    HttpServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val httpFunctions = type.getAnnotationsByType(HttpServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)

        val enabledRequestCompression = httpFunctions.any { it.enableRequestDecoding }
        val enabledResponseCompression = httpFunctions.any { it.enableResponseEncoding }

        val fileBuilder = HttpServerlessFunctionFileBuilder(
            processingEnv,
            methodInformation,
            type,
            classForReflectionService,
            enabledRequestCompression,
            enabledResponseCompression
        )

        fileBuilder.createClass()

        for (httpFunction in httpFunctions) {
            val stages = stageService.determineStages(httpFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)
            processingData.nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {

                val config = FunctionConfig(httpFunction.timeout, httpFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                val annotationCorsOrigin = httpFunction.allowedCorsOrigin
                val referencedWebsite =
                    cfDocuments[stage]!!.updateTemplate.referencedFileStorageBucket(annotationCorsOrigin)

                if (referencedWebsite != null) {
                    functionResource.addEnvVariable(
                        ConstantEnvironmentVariable.NIMBUS_ALLOWED_CORS_ORIGIN,
                        referencedWebsite.getAttr("WebsiteURL")
                    )
                } else {
                    functionResource.addEnvVariable(ConstantEnvironmentVariable.NIMBUS_ALLOWED_CORS_ORIGIN, getAllowedOrigin(stage, processingData, httpFunction))
                }

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource)

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }

    companion object {

        fun getAllowedOrigin(stage: String, processingData: ProcessingData, httpServerlessFunction: HttpServerlessFunction): String  {
            if (httpServerlessFunction.allowedCorsOrigin.isNotBlank()) {
                return httpServerlessFunction.allowedCorsOrigin
            }
            if (processingData.defaultAllowedOrigin[stage] != null) {
                return processingData.defaultAllowedOrigin[stage]!!
            }
            return ""
        }

        fun getAllowedHeaders(stage: String, processingData: ProcessingData, httpServerlessFunction: HttpServerlessFunction): Array<String>  {
            if (httpServerlessFunction.allowedCorsHeaders.isNotEmpty()) {
                return httpServerlessFunction.allowedCorsHeaders
            }
            if (processingData.defaultRequestHeaders[stage] != null) {
                return processingData.defaultRequestHeaders[stage]!!.toTypedArray()
            }
            return Array(0) { "" }
        }

    }
}
