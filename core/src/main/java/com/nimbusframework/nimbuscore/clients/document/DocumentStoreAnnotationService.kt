package com.nimbusframework.nimbuscore.clients.document

import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object DocumentStoreAnnotationService {

    fun <T> getTableName(clazz: Class<T>, stage: String): String {
        val documentStoreAnnotations = clazz.getDeclaredAnnotationsByType(DocumentStoreDefinition::class.java)
        for (documentStoreAnnotation in documentStoreAnnotations) {
            for (annotationStage in documentStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    val name =  if (documentStoreAnnotation.tableName != "") documentStoreAnnotation.tableName else clazz.simpleName
                    return "$name$stage"
                }
            }
        }
        throw InvalidStageException()
    }

}