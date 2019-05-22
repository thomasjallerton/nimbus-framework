package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.BasicServerlessFunctions
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.basic.BasicServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class BasicFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        BasicServerlessFunction::class.java,
        BasicServerlessFunctions::class.java
) {



    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val basicFunctions = type.getAnnotationsByType(BasicServerlessFunction::class.java)
        val methodInformation = extractMethodInformation(type)

        //TODO: Need to generate separate class per stage if necessary
        val cron = basicFunctions.map { it.cron.isNotEmpty() }.reduce { acc, b -> acc || b }

        val fileBuilder = BasicServerlessFunctionFileBuilder(
                cron,
                processingEnv,
                methodInformation,
                type,
                nimbusState
        )

        fileBuilder.createClass()

        val handler = fileBuilder.getHandler()

        for (basicFunction in basicFunctions) {
            for (stage in basicFunction.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationDocuments() }
                val updateResources = cloudFormationDocuments.updateResources

                val handlerInformation = HandlerInformation(
                        handlerClassPath = fileBuilder.classFilePath(),
                        handlerFile = fileBuilder.handlerFile(),
                        replacementVariable = "\${${fileBuilder.handlerFile()}}"
                )

                val config = FunctionConfig(basicFunction.timeout, basicFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                        handler,
                        methodInformation,
                        handlerInformation,
                        config
                )

                //Configure cron if necessary
                if (basicFunction.cron != "") {
                    functionEnvironmentService.newCronTrigger(basicFunction.cron, functionResource)
                }
                updateResources.addInvokableFunction(functionResource)


                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}