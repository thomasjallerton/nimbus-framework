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
import com.nimbusframework.nimbusaws.wrappers.store.document.DocumentStoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.DocumentStoreServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DocumentStoreServerlessFunctionAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class DocumentStoreFunctionResourceCreator(
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
    DocumentStoreServerlessFunction::class.java,
    DocumentStoreServerlessFunctions::class.java
) {

    override fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val documentStoreFunctions = type.getAnnotationsByType(DocumentStoreServerlessFunction::class.java)
        val results = mutableListOf<FunctionInformation>()

        val methodInformation = extractMethodInformation(type)

        var fileWritten = false
        var handlerInformation = HandlerInformation("", "", "")
        var awsMethodInformation = AwsMethodInformation("", "", "")

        for (documentStoreFunction in documentStoreFunctions) {
            val stages = stageService.determineStages(documentStoreFunction.stages)

            val dataModelAnnotation = DocumentStoreServerlessFunctionAnnotation(documentStoreFunction)

            if (!fileWritten) {
                val fileBuilder = DocumentStoreServerlessFunctionFileBuilder<Any>(
                    processingEnv,
                    methodInformation,
                    type,
                    documentStoreFunction.method,
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
                val config = FunctionConfig(documentStoreFunction.timeout, documentStoreFunction.memory, stage)
                val functionResource = functionEnvironmentService.newFunction(
                    methodInformation,
                    handlerInformation,
                    config
                )

                val dynamoResource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, type, stage)

                if (dynamoResource == null) {
                    val dataModelClass = dataModelAnnotation.getTypeElement(processingEnv)
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${dataModelClass.simpleName} is not annotated with a DocumentStore annotation",
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
