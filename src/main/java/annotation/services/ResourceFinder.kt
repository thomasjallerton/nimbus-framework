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

    fun getDocumentStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val documentStores = typeElement.getAnnotationsByType(DocumentStore::class.java)
            for (documentStore in documentStores) {
                for (stage in documentStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource(documentStore.existingArn, documentStore.tableName, typeElement.simpleName.toString(), dataModelStage)
                    }
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find KeyValueStore on @UsesDocumentStore dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod)
            return null
        }
    }

    fun getKeyValueStoreResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): Resource? {
        try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val keyValueStores = typeElement.getAnnotationsByType(KeyValueStore::class.java)
            for (keyValueStore in keyValueStores) {
                for (stage in keyValueStore.stages) {
                    if (stage == dataModelStage) {
                        return getStoreResource(keyValueStore.existingArn, keyValueStore.tableName, typeElement.simpleName.toString(), dataModelStage)
                    }
                }
            }
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find KeyValueStore on @UsesKeyValueStore dataModel with same stage")
            return null
        } catch (e: NullPointerException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod)
            return null
        }
    }

    fun getRelationalDatabaseResource(dataModelAnnotation: DataModelAnnotation, serverlessMethod: Element, dataModelStage: String): RdsResource? {
        return try {
            val typeElement = dataModelAnnotation.getTypeElement(processingEnv)
            val relationalDatabase = typeElement.getAnnotation(RelationalDatabase::class.java)
            return resourceCollections.getValue(dataModelStage).updateResources.get("${relationalDatabase.name}RdsInstance") as RdsResource?
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