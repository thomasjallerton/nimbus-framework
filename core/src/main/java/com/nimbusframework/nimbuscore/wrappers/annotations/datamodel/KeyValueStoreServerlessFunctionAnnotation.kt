package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction

class KeyValueStoreServerlessFunctionAnnotation(private val keyValueStoreServerlessFunction: KeyValueStoreServerlessFunction): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return keyValueStoreServerlessFunction.dataModel.java
    }
}