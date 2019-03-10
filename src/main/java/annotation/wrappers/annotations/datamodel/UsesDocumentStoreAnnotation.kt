package annotation.wrappers.annotations.datamodel

import annotation.annotations.document.UsesDocumentStore

class UsesDocumentStoreAnnotation(private val usesDocumentStore: UsesDocumentStore): DataModelAnnotation() {

    override val stage = usesDocumentStore.stage

    override fun internalDataModel(): Class<out Any> {
        return usesDocumentStore.dataModel.java
    }
}