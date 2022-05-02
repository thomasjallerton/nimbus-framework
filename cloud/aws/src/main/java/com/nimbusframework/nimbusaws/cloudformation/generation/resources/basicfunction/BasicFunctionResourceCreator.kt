package com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.basic.BasicFunctionClientBuilder
import com.nimbusframework.nimbusaws.wrappers.basic.BasicServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.BasicServerlessFunctions
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class BasicFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
    processingEnv,
    decoratorHandlers,
    messager,
    BasicServerlessFunction::class.java,
    BasicServerlessFunctions::class.java
) {

    private val methodsToProcess: MutableMap<Pair<String, String>, MutableSet<FileBuilderMethodInformation>> = mutableMapOf()

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val basicFunctions = type.getAnnotationsByType(BasicServerlessFunction::class.java)
        val methodInformation = extractMethodInformation(type)
        val results = mutableListOf<FunctionInformation>()

        hasEmptyConstructor(type.enclosingElement as TypeElement)

        //TODO: Need to generate separate class per stage if necessary
        val cron = basicFunctions.map { it.cron.isNotEmpty() }.reduce { acc, b -> acc || b }

        val fileBuilder = BasicServerlessFunctionFileBuilder(
            cron,
            processingEnv,
            methodInformation,
            type,
            classForReflectionService
        )

        fileBuilder.createClass()

        methodsToProcess.getOrPut(Pair(methodInformation.className, methodInformation.packageName)) { mutableSetOf() }
            .add(methodInformation)

        for (basicFunction in basicFunctions) {
            val stages = stageService.determineStages(basicFunction.stages)

            val handlerInformation = createHandlerInformation(type, fileBuilder)

            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val config = FunctionConfig(basicFunction.timeout, basicFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                //Configure cron if necessary
                if (basicFunction.cron != "") {
                    functionEnvironmentService.newCronTrigger(basicFunction.cron, functionResource)
                }
                updateResources.addInvokableFunction(
                    methodInformation.className,
                    methodInformation.methodName,
                    functionResource
                )

                results.add(FunctionInformation(type, functionResource, fileBuilder.getGeneratedClassInformation()))
            }
        }
        return results
    }

    override fun afterAllElements() {
        for (classToProcess in methodsToProcess) {
            val classInformation = classToProcess.key
            BasicFunctionClientBuilder(
                classInformation.first,
                classInformation.second,
                classToProcess.value,
                processingEnv
            ).writeInterfaceClass()
        }
    }
}
