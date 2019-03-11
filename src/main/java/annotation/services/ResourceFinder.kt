package annotation.services

import annotation.annotations.database.RelationalDatabase
import annotation.annotations.document.DocumentStore
import annotation.annotations.keyvalue.KeyValueStore
import annotation.wrappers.annotations.datamodel.DataModelAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.ExistingResource
import cloudformation.resource.Resource
import cloudformation.resource.database.RdsResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class ResourceFinder(private val resourceCollections: Map<String, CloudFormationDocuments>, private val processingEnv: ProcessingEnvironment, private val nimbusState: NimbusState) {

    private val messager = processingEnv.messager

    fun getDocumentStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val documentStores = typeElement.getAnnotationsByType(DocumentStore::class.java)
            for (documentStore in documentStores) {
                if (documentStore.stage == dataModelAnnotation.stage) {
                    return getStoreResource(documentStore.existingArn, documentStore.tableName, typeElement.simpleName.toString(), dataModelAnnotation.stage)
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find KeyValueStore on @UsesDocumentStore dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod)
            return null
        }
    }

    fun getKeyValueStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val keyValueStores = typeElement.getAnnotationsByType(KeyValueStore::class.java)
            for (keyValueStore in keyValueStores) {
                if (keyValueStore.stage == dataModelAnnotation.stage) {
                    return getStoreResource(keyValueStore.existingArn, keyValueStore.tableName, typeElement.simpleName.toString(), dataModelAnnotation.stage)
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find KeyValueStore on @UsesKeyValueStore dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod)
            return null
        }
    }

    fun getRelationalDatabaseResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element): RdsResource? {
        return try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val relationalDatabase = typeElement.getAnnotation(RelationalDatabase::class.java)
            return resourceCollections.getValue(dataModelAnnotation.stage).updateResources.get("${relationalDatabase.name}RdsInstance") as RdsResource?
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod)
            null
        }
    }

    private fun getStoreResource(existingArn: String, tableName: String, elementName: String, stage: String): Resource? {
        return if (existingArn == "") {
            return resourceCollections.getValue(stage).updateResources.get(determineTableName(tableName, elementName, stage))
        } else {
            ExistingResource(existingArn, nimbusState, stage)
        }
    }

    private fun determineTableName(givenName: String, className: String, stage: String): String {
        return if (givenName == "") {
            "$className$stage"
        } else {
            "$givenName$stage"
        }
    }


}