package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionPermissionResource
import com.nimbusframework.nimbusaws.wrappers.notification.NotificationServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.NotificationServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.NotificationTopicServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class NotificationFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        private val processingEnv: ProcessingEnvironment,
        private val messager: Messager,
        private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        NotificationServerlessFunction::class.java,
        NotificationServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val notificationFunctions = type.getAnnotationsByType(NotificationServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)

        val fileBuilder = NotificationServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        for (notificationFunction in notificationFunctions) {
            val stages = stageService.determineStages(notificationFunction.stages)

            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = stages
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(notificationFunction.timeout, notificationFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        fileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                val snsTopic = resourceFinder.getNotificationTopicResource(NotificationTopicServerlessFunctionAnnotation(notificationFunction), type, stage)

                if (snsTopic == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find notification topic class", type)
                    return
                }

                val updateResources = cfDocuments[stage]!!.updateTemplate.resources

                snsTopic.setFunction(functionResource)
                updateResources.addResource(snsTopic)

                val permission = FunctionPermissionResource(functionResource, snsTopic, nimbusState)
                updateResources.addResource(permission)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }

    }

}