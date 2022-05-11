package com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.wrappers.notification.NotificationServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.NotificationServerlessFunctions
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.NotificationTopicServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class NotificationFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager,
    private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
    messager,
    NotificationServerlessFunction::class.java,
    NotificationServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val notificationFunctions = type.getAnnotationsByType(NotificationServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)

        val fileBuilder = NotificationServerlessFunctionFileBuilder(
            processingEnv,
            methodInformation,
            type,
            classForReflectionService
        )

        for (notificationFunction in notificationFunctions) {
            val stages = stageService.determineStages(notificationFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(notificationFunction.timeout, notificationFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                val snsTopic = resourceFinder.getNotificationTopicResource(
                    NotificationTopicServerlessFunctionAnnotation(notificationFunction), type, stage
                )

                if (snsTopic == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find notification topic class", type)
                    return listOf()
                }

                val updateResources = cfDocuments[stage]!!.updateTemplate.resources

                snsTopic.setFunction(functionResource)

                val permission = FunctionPermissionResource(functionResource, snsTopic, nimbusState)
                updateResources.addResource(permission)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }

}
