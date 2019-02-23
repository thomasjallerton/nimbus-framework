package annotation.services

import annotation.annotations.document.DocumentStore
import annotation.annotations.keyvalue.KeyValueStore
import cloudformation.persisted.NimbusState
import cloudformation.resource.ExistingResource
import cloudformation.resource.Resource
import cloudformation.resource.ResourceCollection
import annotation.wrappers.annotations.datamodel.DataModelAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class ResourceFinder(private val resourceCollection: ResourceCollection, private val processingEnv: ProcessingEnvironment, private val nimbusState: NimbusState) {

    private val messager = processingEnv.messager

    fun getDocumentStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element): Resource? {
        return try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val documentStore = typeElement.getAnnotation(DocumentStore::class.java)
            getResource(resourceCollection, documentStore.existingArn, documentStore.tableName, typeElement.simpleName.toString())
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod)
            null
        }

    }

    fun getKeyValueStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element): Resource? {
        return try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val keyValueStore = typeElement.getAnnotation(KeyValueStore::class.java)
            getResource(resourceCollection, keyValueStore.existingArn, keyValueStore.tableName, typeElement.simpleName.toString())
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod)
            null
        }

    }

    private fun getResource(updateResources: ResourceCollection, existingArn: String, tableName: String, elementName: String): Resource? {
        return if (existingArn == "") {
            updateResources.get(determineTableName(tableName, elementName))
        } else {
            ExistingResource(existingArn, nimbusState)
        }
    }

    private fun determineTableName(givenName: String, className: String): String {
        return if (givenName == "") {
            className
        } else {
            givenName
        }
    }


}