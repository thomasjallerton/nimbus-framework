package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.KeyValueStoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.KeyValueStoreServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.KeyValueStoreServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class KeyValueStoreFunctionResourceCreator(
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    processingData: ProcessingData,
    private val classForReflectionService: ClassForReflectionService,
    processingEnv: ProcessingEnvironment,
    decoratorHandlers: Set<FunctionDecoratorHandler>,
    messager: Messager,
    private val resourceFinder: ResourceFinder
) : FunctionResourceCreator(
    cfDocuments,
    processingData,
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
        var handlerInformation = HandlerInformation("", "", "")
        var awsMethodInformation = AwsMethodInformation("", "", "")

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
                    classForReflectionService
                )

                fileBuilder.createClass()
                fileWritten = true
                handlerInformation = createHandlerInformation(type, fileBuilder)
                nimbusState.handlerFiles.add(handlerInformation)
                awsMethodInformation = fileBuilder.getGeneratedClassInformation()
            }

            for (stage in stages) {
                val config = FunctionConfig(keyValueStoreFunction.timeout, keyValueStoreFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
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

                results.add(FunctionInformation(type, functionResource, awsMethodInformation))
            }
        }
        return results
    }
}
