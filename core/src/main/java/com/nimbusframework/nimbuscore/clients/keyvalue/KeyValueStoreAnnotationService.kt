package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object KeyValueStoreAnnotationService {

    fun <T> getTableName(clazz: Class<T>, stage: String): String {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStoreDefinition::class.java)
        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            for (annotationStage in keyValueStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    val name = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
                    return "$name$stage"
                }
            }
        }
        throw InvalidStageException()
    }

    fun <T> getKeyNameAndType(clazz: Class<T>, stage: String): Pair<String, Class<*>> {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStoreDefinition::class.java)
        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            for (annotationStage in keyValueStoreAnnotation.stages) {
                if (annotationStage == stage) {
                    return Pair(keyValueStoreAnnotation.keyName, keyValueStoreAnnotation.keyType.java)
                }
            }
        }
        throw InvalidStageException()
    }

}