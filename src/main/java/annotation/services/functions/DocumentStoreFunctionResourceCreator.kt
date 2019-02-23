package annotation.services.functions

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.resource.ResourceCollection
import annotation.cloudformation.resource.function.FunctionConfig
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import annotation.wrappers.annotations.datamodel.DocumentStoreServerlessFunctionAnnotation
import wrappers.document.DocumentStoreServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind

class DocumentStoreFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(updateResources, nimbusState, processingEnv) {

    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(DocumentStoreServerlessFunction::class.java)

        val results = LinkedList<FunctionInformation>()
        for (type in annotatedElements) {
            val documentStoreFunction = type.getAnnotation(DocumentStoreServerlessFunction::class.java)

            if (type.kind == ElementKind.METHOD) {
                val methodInformation = extractMethodInformation(type)
                val dataModelAnnotation = DocumentStoreServerlessFunctionAnnotation(documentStoreFunction)

                val fileBuilder = DocumentStoreServerlessFunctionFileBuilder<Any>(
                        processingEnv,
                        methodInformation,
                        type,
                        documentStoreFunction.method,
                        dataModelAnnotation.getTypeElement(processingEnv)
                )

                val handler = fileBuilder.getHandler()

                val config = FunctionConfig(documentStoreFunction.timeout, documentStoreFunction.memory)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                val dynamoResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, type)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource!!, functionResource)
                }

                fileBuilder.createClass()

                results.add(FunctionInformation(type, functionResource))
            }
        }
        return results
    }
}