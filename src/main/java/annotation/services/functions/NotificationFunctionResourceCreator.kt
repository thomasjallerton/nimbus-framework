package annotation.services.functions

import annotation.annotations.function.NotificationServerlessFunction
import annotation.annotations.function.repeatable.NotificationServerlessFunctions
import persisted.NimbusState
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import wrappers.notification.NotificationServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

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
                type
        )

        for (notificationFunction in notificationFunctions) {
            for (stage in notificationFunction.stages) {
                val config = FunctionConfig(notificationFunction.timeout, notificationFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config)

                functionEnvironmentService.newNotification(notificationFunction, functionResource)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }

    }

}