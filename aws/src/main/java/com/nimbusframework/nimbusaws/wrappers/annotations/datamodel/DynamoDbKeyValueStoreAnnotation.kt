package com.nimbusframework.nimbusaws.wrappers.annotations.datamodel

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation


class DynamoDbKeyValueStoreAnnotation(private val keyValueStore: DynamoDbKeyValueStore): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return keyValueStore.keyType.java
    }

}