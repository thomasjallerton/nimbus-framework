package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactory

class CustomFactoryAnnotation(private val customFactory: CustomFactory): DataModelAnnotation() {
    override fun internalDataModel(): Class<out Any> {
        return customFactory.value.java
    }
}