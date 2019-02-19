package annotation.wrappers

import annotation.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation {

    override fun getDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }
}