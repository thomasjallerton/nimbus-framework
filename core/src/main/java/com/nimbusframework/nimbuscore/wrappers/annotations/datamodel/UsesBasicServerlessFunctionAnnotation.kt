package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction

class UsesBasicServerlessFunctionAnnotation(private val usesBasicServerlessFunctionAnnotation: UsesBasicServerlessFunction): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return usesBasicServerlessFunctionAnnotation.targetClass.java
    }
}