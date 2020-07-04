package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction

class DocumentStoreServerlessFunctionAnnotation(private val documentStoreFunction: DocumentStoreServerlessFunction): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return documentStoreFunction.dataModel.java
    }

}