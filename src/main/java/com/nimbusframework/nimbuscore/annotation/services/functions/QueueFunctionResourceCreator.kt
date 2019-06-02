package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.QueueServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
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
            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = queueFunction.stages.toSet()
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in queueFunction.stages) {
                val config = FunctionConfig(queueFunction.timeout, queueFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        fileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                functionEnvironmentService.newQueue(queueFunction, functionResource)

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }

}