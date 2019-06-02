package com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotation.annotations.function.UsesBasicServerlessFunctionClient

class UsesBasicServerlessFunctionAnnotation(private val usesBasicServerlessFunctionAnnotation: UsesBasicServerlessFunctionClient): DataModelAnnotation() {

    override val stages: Array<String> = usesBasicServerlessFunctionAnnotation.stages

    override fun internalDataModel(): Class<out Any> {
        return usesBasicServerlessFunctionAnnotation.targetClass.java
    }
}