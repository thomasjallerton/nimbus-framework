package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionEventMappingResource
import com.nimbusframework.nimbusaws.wrappers.queue.QueueServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.QueueServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.QueueServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class QueueFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment,
        messager: Messager,
        private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        messager,
        QueueServerlessFunction::class.java,
        QueueServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val queueFunctions = type.getAnnotationsByType(QueueServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)

        val fileBuilder = QueueServerlessFunctionFileBuilder(
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        for (queueFunction in queueFunctions) {
            val stages = stageService.determineStages(queueFunction.stages)

            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = stages
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(queueFunction.timeout, queueFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        fileBuilder.getHandler(),
                        methodInformation,
                        handlerInformation,
                        config
                )

                val sqsQueue = resourceFinder.getQueueResource(QueueServerlessFunctionAnnotation(queueFunction), type, stage)

                if (sqsQueue == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find queue class", type)
                    return listOf()
                }

                val eventMapping = FunctionEventMappingResource(
                        sqsQueue.getArn(""),
                        sqsQueue.getName(),
                        queueFunction.batchSize,
                        functionResource,
                        false,
                        nimbusState
                )

                val updateResources = cfDocuments[stage]!!.updateTemplate.resources

                updateResources.addResource(eventMapping)

                val iamRoleResource = functionResource.getIamRoleResource()

                iamRoleResource.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
                iamRoleResource.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
                iamRoleResource.addAllowStatement("sqs:GetQueueAttributes", sqsQueue, "")

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results
    }

}