package annotation.services.functions

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import annotation.wrappers.annotations.datamodel.DocumentStoreServerlessFunctionAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionConfig
import persisted.NimbusState
import wrappers.store.document.DocumentStoreServerlessFunctionFileBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind

class DocumentStoreFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        DocumentStoreServerlessFunction::class.java,
        DocumentStoreServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>) {
        val documentStoreFunctions = type.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)

        val methodInformation = extractMethodInformation(type)

        var fileWritten = false
        var handler = ""

        for (documentStoreFunction in documentStoreFunctions) {
            val dataModelAnnotation = DocumentStoreServerlessFunctionAnnotation(documentStoreFunction)

            if (!fileWritten) {
                val fileBuilder = DocumentStoreServerlessFunctionFileBuilder<Any>(
                        processingEnv,
                        methodInformation,
                        type,
                        documentStoreFunction.method,
                        dataModelAnnotation.getTypeElement(processingEnv)
                )

                handler = fileBuilder.getHandler()
                fileBuilder.createClass()
                fileWritten = true
            }

            for (stage in documentStoreFunction.stages) {
                val config = FunctionConfig(documentStoreFunction.timeout, documentStoreFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config)

                val dynamoResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)
                }


                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}