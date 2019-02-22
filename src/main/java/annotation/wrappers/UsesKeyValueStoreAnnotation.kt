package annotation.wrappers

import annotation.annotations.keyvalue.UsesKeyValueStore

class UsesKeyValueStoreAnnotation(private val usesKeyValueStoreAnnotation: UsesKeyValueStore): DataModelAnnotation() {
    override fun internalDataModel(): Class<out Any> {
        return usesKeyValueStoreAnnotation.dataModel.java
    }

}