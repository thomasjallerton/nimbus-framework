package annotation.services.functions

import annotation.annotations.function.NotificationServerlessFunction
import annotation.models.persisted.NimbusState
import annotation.models.resource.ResourceCollection
import annotation.models.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import wrappers.notification.NotificationServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind

class NotificationFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(updateResources, nimbusState, processingEnv) {


    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(NotificationServerlessFunction::class.java)

        val results = LinkedList<FunctionInformation>()

        for (type in annotatedElements) {
            val notificationFunction = type.getAnnotation(NotificationServerlessFunction::class.java)

            if (type.kind == ElementKind.METHOD) {
                val methodInformation = extractMethodInformation(type)

                val fileBuilder = NotificationServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                )

                val config = FunctionConfig(notificationFunction.timeout, notificationFunction.memory)
                val functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config)

                functionEnvironmentService.newNotification(notificationFunction, functionResource)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results    }
}