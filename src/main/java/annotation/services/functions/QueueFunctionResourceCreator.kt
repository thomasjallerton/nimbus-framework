package annotation.services.functions

import annotation.annotations.function.QueueServerlessFunction
import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import annotation.models.resource.ResourceCollection
import annotation.models.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import wrappers.queue.QueueServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.tools.Diagnostic

class QueueFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment,
        private val savedResources: MutableMap<String, Resource>
) : FunctionResourceCreator(updateResources, nimbusState, processingEnv) {
    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(QueueServerlessFunction::class.java)

        val results = LinkedList<FunctionInformation>()

        for (type in annotatedElements) {
            val queueFunction = type.getAnnotation(QueueServerlessFunction::class.java)

            if (type.kind == ElementKind.METHOD) {
                val methodInformation = extractMethodInformation(type)

                val fileBuilder = QueueServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                )

                val config = FunctionConfig(queueFunction.timeout, queueFunction.memory)
                val functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config)

                val newQueue = functionEnvironmentService.newQueue(queueFunction, functionResource)

                if (savedResources.containsKey(queueFunction.id)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can't have multiple consumers of the same queue ("
                            + queueFunction.id + ")", type)
                    return results
                }
                savedResources[queueFunction.id] = newQueue

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results    }
}