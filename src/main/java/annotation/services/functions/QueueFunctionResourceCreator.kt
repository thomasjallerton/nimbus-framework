package annotation.services.functions

import annotation.annotations.function.QueueServerlessFunction
import annotation.annotations.function.repeatable.QueueServerlessFunctions
import persisted.NimbusState
import cloudformation.resource.Resource
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import cloudformation.CloudFormationDocuments
import wrappers.queue.QueueServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.tools.Diagnostic

class QueueFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        QueueServerlessFunction::class.java,
        QueueServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val queueFunctions = type.getAnnotationsByType(QueueServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)

        val fileBuilder = QueueServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type
        )

        for (queueFunction in queueFunctions) {
            for (stage in queueFunction.stages) {
                val config = FunctionConfig(queueFunction.timeout, queueFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config)

                val newQueue = functionEnvironmentService.newQueue(queueFunction, functionResource)

                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val savedResources = cloudFormationDocuments.savedResources

                if (savedResources.containsKey(queueFunction.id)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can't have multiple consumers of the same queue ("
                            + queueFunction.id + ")", type)
                    return
                }
                savedResources[queueFunction.id] = newQueue

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }

}