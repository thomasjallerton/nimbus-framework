package annotation.wrappers.annotations.datamodel

import annotation.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation() {

    override val stages: Array<String> = documentStoreFunction.stages

    override fun internalDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }
}