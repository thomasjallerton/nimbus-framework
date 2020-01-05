package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition

class KeyValueStoreAnnotation(private val keyValueStore: KeyValueStoreDefinition): DataModelAnnotation() {

    override val stages: Array<String> = keyValueStore.stages

    override fun internalDataModel(): Class<out Any> {
        return keyValueStore.keyType.java
    }
}