package annotation.wrappers.annotations.datamodel

import annotation.annotations.keyvalue.KeyValueStore

class KeyValueStoreAnnotation(private val keyValueStore: KeyValueStore): DataModelAnnotation() {
    override fun internalDataModel(): Class<out Any> {
        return keyValueStore.keyType.java
    }
}