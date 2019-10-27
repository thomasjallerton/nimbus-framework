package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.KeyValueStoreServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.wrappers.store.keyvalue.KeyValueStoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.KeyValueStoreServerlessFunctionAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class KeyValueStoreFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        KeyValueStoreServerlessFunction::class.java,
        KeyValueStoreServerlessFunctions::class.java
) {


    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val keyValueStoreFunctions = type.getAnnotationsByType(KeyValueStoreServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)

        var fileWritten = false
        var handler = ""
        var handlerInformation = HandlerInformation()


        for (keyValueStoreFunction in keyValueStoreFunctions) {
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
                        stages = keyValueStoreFunction.stages.toSet()
                )
                nimbusState.handlerFiles.add(handlerInformation)

            }

            for (stage in keyValueStoreFunction.stages) {
                val config = FunctionConfig(keyValueStoreFunction.timeout, keyValueStoreFunction.memory, stage)

                val functionResource = functionEnvironmentService.newFunction(
                        handler,
                        methodInformation,
                        handlerInformation,
                        config
                )

                val dynamoResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)
                }

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}