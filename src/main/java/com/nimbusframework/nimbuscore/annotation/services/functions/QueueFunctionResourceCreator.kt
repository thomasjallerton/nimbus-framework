package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.QueueServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.wrappers.queue.QueueServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
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
                type,
                nimbusState
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