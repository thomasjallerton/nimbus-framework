package com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation() {

    override val stages: Array<String> = documentStoreFunction.stages

    override fun internalDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }
}