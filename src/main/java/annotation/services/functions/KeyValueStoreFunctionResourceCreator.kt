package annotation.services.functions

import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.resource.ResourceCollection
import annotation.cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import annotation.wrappers.annotations.datamodel.KeyValueStoreServerlessFunctionAnnotation
import wrappers.store.keyvalue.KeyValueStoreServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind

class KeyValueStoreFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(updateResources, nimbusState, processingEnv) {


    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(KeyValueStoreServerlessFunction::class.java)

        val results = LinkedList<FunctionInformation>()
        for (type in annotatedElements) {
            val keyValueStoreFunction = type.getAnnotation(KeyValueStoreServerlessFunction::class.java)

            if (type.kind == ElementKind.METHOD) {
                val methodInformation = extractMethodInformation(type)
                val dataModelAnnotation = KeyValueStoreServerlessFunctionAnnotation(keyValueStoreFunction)

                val fileBuilder = KeyValueStoreServerlessFunctionFileBuilder<Any>(
                        processingEnv,
                        methodInformation,
                        type,
                        keyValueStoreFunction.method,
                        dataModelAnnotation.getTypeElement(processingEnv)
                )

                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(keyValueStoreFunction.timeout, keyValueStoreFunction.memory)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                val dynamoResource = resourceFinder.getKeyValueStoreResource(dataModelAnnotation, type)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)
                }

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results    }

}