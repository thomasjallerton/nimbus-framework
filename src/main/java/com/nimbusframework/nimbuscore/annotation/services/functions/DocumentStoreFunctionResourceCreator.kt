package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions
import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.DocumentStoreServerlessFunctionAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionConfig
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.store.document.DocumentStoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

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
        var handlerInformation = HandlerInformation()


        for (documentStoreFunction in documentStoreFunctions) {
            val dataModelAnnotation = DocumentStoreServerlessFunctionAnnotation(documentStoreFunction)

            if (!fileWritten) {
                val fileBuilder = DocumentStoreServerlessFunctionFileBuilder<Any>(
                        processingEnv,
                        methodInformation,
                        type,
                        documentStoreFunction.method,
                        dataModelAnnotation.getTypeElement(processingEnv),
                        nimbusState
                )

                handler = fileBuilder.getHandler()
                fileBuilder.createClass()
                fileWritten = true
                handlerInformation = HandlerInformation(handlerClassPath = fileBuilder.classFilePath(), handlerFile = fileBuilder.handlerFile())

            }

            for (stage in documentStoreFunction.stages) {
                val config = FunctionConfig(documentStoreFunction.timeout, documentStoreFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                        handler,
                        methodInformation,
                        handlerInformation,
                        config
                )

                val dynamoResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource != null) {
                    functionEnvironmentService.newStoreTrigger(dynamoResource, functionResource)
                }


                results.add(FunctionInformation(type, functionResource))
            }
        }
    }
}