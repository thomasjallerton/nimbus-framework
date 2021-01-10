package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.KeyValueStoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.KeyValueStoreServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.KeyValueStoreServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class KeyValueStoreFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    nimbusState: NimbusState,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager,
    private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
    cfDocuments,
    nimbusState,
    processingEnv,
    decoratorHandlers,
    messager,
    KeyValueStoreServerlessFunction::class.java,
    KeyValueStoreServerlessFunctions::class.java
) {


    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val keyValueStoreFunctions = type.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)

        var fileWritten = false
        var handler = ""
        var handlerInformation = HandlerInformation()


        for (keyValueStoreFunction in keyValueStoreFunctions) {
            val stages = stageService.determineStages(keyValueStoreFunction.stages)

            val dataModelAnnotation = KeyValueStoreServerlessFunctionAnnotation(keyValueStoreFunction)

            if (!fileWritten) {
                val fileBuilder = KeyValueStoreServerlessFunctionFileBuilder<Any>(
                    processingEnv,
                    methodInformation,
                    type,
                    keyValueStoreFunction.method,
                    dataModelAnnotation.getTypeElement(processingEnv),
                    nimbusState
                )

                handler = fileBuilder.getHandler()
                fileBuilder.createClass()
                fileWritten = true
                handlerInformation = HandlerInformation(
                    handlerClassPath = fileBuilder.classFilePath(),
                    handlerFile = fileBuilder.handlerFile(),
                    replacementVariable = "\${${fileBuilder.handlerFile()}}",
                    stages = stages
                )
                nimbusState.handlerFiles.add(handlerInformation)

            }

            for (stage in stages) {
                val config = FunctionConfig(keyValueStoreFunction.timeout, keyValueStoreFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                    handler,
                    methodInformation,
                    handlerInformation,
                    config
                )

                val dynamoResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource == null) {
                    val dataModelClass = dataModelAnnotation.getTypeElement(processingEnv)
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${dataModelClass.simpleName} is not annotated with a KeyValueStore annotation",
                        type
                    )
                    return listOf()
                }

                functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results
    }
}