package com.nimbusframework.nimbusaws.clients.document

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object DynamoDbDocumentStoreAnnotationService {

    fun <T> getTableName(clazz: Class<T>, stage: String): String {
        val documentStoreAnnotations = clazz.getAnnotationsByType(DynamoDbDocumentStore::class.java)
        for (documentStoreAnnotation in documentStoreAnnotations) {
            for (annotationStage in documentStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    val name = if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
                    return "$name$stage"
                }
            }
        }
        throw InvalidStageException()
    }

}