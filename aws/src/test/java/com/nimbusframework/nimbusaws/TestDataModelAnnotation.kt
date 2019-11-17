package com.nimbusframework.nimbusaws

import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class TestDataModelAnnotation(private val typeElem: TypeElement, override val stages: Array<String>): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return Any::class.java
    }

    override fun getTypeElement(processingEnv: ProcessingEnvironment): TypeElement {
        return typeElem
    }
}