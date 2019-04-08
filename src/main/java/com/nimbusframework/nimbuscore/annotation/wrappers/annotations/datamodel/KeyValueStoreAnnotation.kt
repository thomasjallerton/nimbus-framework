package com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore

class KeyValueStoreAnnotation(private val keyValueStore: KeyValueStore): DataModelAnnotation() {

    override val stages: Array<String> = keyValueStore.stages

    override fun internalDataModel(): Class<out Any> {
        return keyValueStore.keyType.java
    }
}