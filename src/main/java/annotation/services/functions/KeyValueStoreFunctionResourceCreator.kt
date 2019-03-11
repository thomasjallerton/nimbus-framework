package annotation.services.functions

import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.annotations.function.repeatable.KeyValueStoreServerlessFunctions
import persisted.NimbusState
import cloudformation.resource.ResourceCollection
import cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import annotation.wrappers.annotations.datamodel.KeyValueStoreServerlessFunctionAnnotation
import cloudformation.CloudFormationDocuments
import wrappers.store.keyvalue.KeyValueStoreServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class KeyValueStoreFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
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

        for (keyValueStoreFunction in keyValueStoreFunctions) {
            val dataModelAnnotation = KeyValueStoreServerlessFunctionAnnotation(keyValueStoreFunction)

            if (!fileWritten) {
                val fileBuilder = KeyValueStoreServerlessFunctionFileBuilder<Any>(
                        processingEnv,
                        methodInformation,
                        type,
                        keyValueStoreFunction.method,
                        dataModelAnnotation.getTypeElement(processingEnv)
                )

                handler = fileBuilder.getHandler()
                fileBuilder.createClass()
                fileWritten = true
            }

            for (stage in keyValueStoreFunction.stages) {
                val config = FunctionConfig(keyValueStoreFunction.timeout, keyValueStoreFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                val dynamoResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)
                }

                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}