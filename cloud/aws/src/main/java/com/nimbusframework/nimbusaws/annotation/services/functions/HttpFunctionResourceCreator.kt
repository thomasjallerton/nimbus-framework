package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.http.HttpServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.NimbusConstants.allowedOriginEnvVariable
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
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

        val fileBuilder = HttpServerlessFunctionFileBuilder(
            processingEnv,
            methodInformation,
            type,
            classForReflectionService
        )

        fileBuilder.createClass()

        for (httpFunction in httpFunctions) {
            val stages = stageService.determineStages(httpFunction.stages)

            val handlerInformation = HandlerInformation(
                handlerClassPath = fileBuilder.classFilePath(),
                handlerFile = fileBuilder.handlerFile(),
                replacementVariable = "\${${fileBuilder.handlerFile()}}",
                stages = stages
            )
            processingData.nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(httpFunction.timeout, httpFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                    handler,
                    methodInformation,
                    handlerInformation,
                    config
                )

                val annotationCorsOrigin = httpFunction.allowedCorsOrigin
                val referencedWebsite =
                    cfDocuments[stage]!!.updateTemplate.referencedFileStorageBucket(annotationCorsOrigin)

                if (referencedWebsite != null) {
                    functionResource.addEnvVariable(
                        allowedOriginEnvVariable,
                        referencedWebsite.getAttr("WebsiteURL")
                    )
                } else {
                    functionResource.addEnvVariable(allowedOriginEnvVariable, getAllowedOrigins(stage, processingData.nimbusState, httpFunction))
                }

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource)

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }

    companion object {

        fun getAllowedOrigins(stage: String, nimbusState: NimbusState, httpServerlessFunction: HttpServerlessFunction): String  {
            if (httpServerlessFunction.allowedCorsOrigin.isNotBlank()) {
                return httpServerlessFunction.allowedCorsOrigin
            }
            if (nimbusState.defaultAllowedOrigin[stage] != null) {
                return nimbusState.defaultAllowedOrigin[stage]!!
            }
            return ""
        }

        fun getAllowedHeaders(stage: String, nimbusState: NimbusState, httpServerlessFunction: HttpServerlessFunction): Array<String>  {
            if (httpServerlessFunction.allowedCorsHeaders.isNotEmpty()) {
                return httpServerlessFunction.allowedCorsHeaders
            }
            if (nimbusState.defaultRequestHeaders[stage] != null) {
                return nimbusState.defaultRequestHeaders[stage]!!.toTypedArray()
            }
            return Array(0) { "" }
        }

    }
}
