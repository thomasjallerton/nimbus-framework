package localDeployment.exampleHandlers

import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.annotations.persistent.StoreUpdate
import localDeployment.exampleModels.KeyValue

class ExampleKeyValueHandler {

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreUpdate.INSERT)
    fun handleInsert(newDocument: KeyValue): Boolean {
        return true
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreUpdate.MODIFY)
    fun handleModify(oldDocument: KeyValue, newDocument: KeyValue): Boolean {
        return true
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue::class, method = StoreUpdate.REMOVE)
    fun handleRemove(oldDocument: KeyValue): Boolean {
        return true
    }
}