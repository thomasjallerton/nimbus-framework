package annotation.wrappers.annotations.datamodel

import annotation.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }
}