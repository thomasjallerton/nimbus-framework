package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object KeyValueStoreAnnotationService {

    fun <T> getTableName(clazz: Class<T>, stage: String): String {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStoreDefinition::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            if (keyValueStoreAnnotation.stages.contains(stage)) {
                val name = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
                return "$name$stage"
            }
        }
        val keyValueStoreAnnotation = keyValueStoreAnnotations.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        val name = if (keyValueStoreAnnotation.tableName != "") keyValueStoreAnnotation.tableName else clazz.simpleName
        return "$name$stage"
    }

    fun <T> getKeyNameAndType(clazz: Class<T>, stage: String): Pair<String, Class<*>> {
        val keyValueStoreAnnotations = clazz.getAnnotationsByType(KeyValueStoreDefinition::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (keyValueStoreAnnotation in keyValueStoreAnnotations) {
            if (keyValueStoreAnnotation.stages.contains(stage)) {
                return Pair(keyValueStoreAnnotation.keyName, keyValueStoreAnnotation.keyType.java)
            }
        }
        val keyValueStoreAnnotation = keyValueStoreAnnotations.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        return Pair(keyValueStoreAnnotation.keyName, keyValueStoreAnnotation.keyType.java)
    }

}