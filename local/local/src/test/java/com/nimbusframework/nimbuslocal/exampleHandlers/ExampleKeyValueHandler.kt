package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue

class ExampleKeyValueHandler {

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreEventType.INSERT)
    fun handleInsert(newDocument: KeyValue): Boolean {
        return true
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreEventType.MODIFY)
    fun handleModify(oldDocument: KeyValue, newDocument: KeyValue): Boolean {
        return true
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreEventType.REMOVE)
    fun handleRemove(oldDocument: KeyValue): Boolean {
        return true
    }
}