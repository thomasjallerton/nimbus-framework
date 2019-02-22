package annotation.wrappers

import annotation.annotations.document.UsesDocumentStore

class UsesDocumentStoreAnnotation(private val usesDocumentStore: UsesDocumentStore): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return usesDocumentStore.dataModel.java
    }
}