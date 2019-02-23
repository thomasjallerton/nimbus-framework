package annotation.wrappers.annotations.datamodel

import annotation.annotations.function.KeyValueStoreServerlessFunction

class KeyValueStoreServerlessFunctionAnnotation(private val keyValueStoreServerlessFunction: KeyValueStoreServerlessFunction): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return keyValueStoreServerlessFunction.dataModel.java
    }
}