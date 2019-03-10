package annotation.wrappers.annotations.datamodel

import annotation.annotations.keyvalue.UsesKeyValueStore

class UsesKeyValueStoreAnnotation(private val usesKeyValueStoreAnnotation: UsesKeyValueStore): DataModelAnnotation() {

    override val stage: String = usesKeyValueStoreAnnotation.stage

    override fun internalDataModel(): Class<out Any> {
        return usesKeyValueStoreAnnotation.dataModel.java
    }

}