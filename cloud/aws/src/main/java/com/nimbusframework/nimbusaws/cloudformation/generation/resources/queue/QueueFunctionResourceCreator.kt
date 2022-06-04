package com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionEventMappingResource
import com.nimbusframework.nimbusaws.wrappers.queue.QueueServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.QueueServerlessFunctions
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.QueueServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class QueueFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    processingEnv: ProcessingEnvironment,
    private val classForReflectionService: ClassForReflectionService,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager,
    private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
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
            classForReflectionService
        )

        fileBuilder.createClass()

        for (queueFunction in queueFunctions) {
            val stages = stageService.determineStages(queueFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)

            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val config = FunctionConfig(queueFunction.timeout, queueFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
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

                val iamRoleResource = functionResource.iamRoleResource

                iamRoleResource.addAllowStatement("sqs:ReceiveMessage", sqsQueue, "")
                iamRoleResource.addAllowStatement("sqs:DeleteMessage", sqsQueue, "")
                iamRoleResource.addAllowStatement("sqs:GetQueueAttributes", sqsQueue, "")

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }

}
