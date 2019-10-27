package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation() {

    override val stages: Array<String> = documentStoreFunction.stages

    override fun internalDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }
}