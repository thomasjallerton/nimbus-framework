package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.BasicServerlessFunctions
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.basic.BasicFunctionClientBuilder
import com.nimbusframework.nimbusaws.wrappers.basic.BasicServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class BasicFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        private val processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        BasicServerlessFunction::class.java,
        BasicServerlessFunctions::class.java
) {

    private val methodsToProcess: MutableMap<Pair<String, String>, MutableSet<MethodInformation>> = mutableMapOf()

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

        methodsToProcess.getOrPut(Pair(methodInformation.className, methodInformation.packageName)) {mutableSetOf()}.add(methodInformation)

        val handler = fileBuilder.getHandler()

        for (basicFunction in basicFunctions) {
            val handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = basicFunction.stages.toSet()
            )
            nimbusState.handlerFiles.add(handlerInformation)

            for (stage in basicFunction.stages) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

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
                updateResources.addInvokableFunction(methodInformation.className, methodInformation.methodName, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }

    override fun afterAllElements() {
        for (classToProcess in methodsToProcess) {
            val classInformation = classToProcess.key
            BasicFunctionClientBuilder(classInformation.first, classInformation.second, classToProcess.value, processingEnv).writeInterfaceClass()
        }
    }
}