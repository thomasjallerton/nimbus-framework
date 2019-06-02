package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.NotificationServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.notification.NotificationServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class NotificationFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
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
            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = notificationFunction.stages.toSet()
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in notificationFunction.stages) {
                val config = FunctionConfig(notificationFunction.timeout, notificationFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        fileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                functionEnvironmentService.newNotification(notificationFunction, functionResource)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }

    }

}