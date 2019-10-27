package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.PrimitiveType

abstract class DataModelAnnotation {

    abstract val stages: Array<String>
    protected abstract fun internalDataModel(): Class<out Any>

    fun getTypeElement(processingEnv: ProcessingEnvironment): TypeElement {
        try {
            val dataModel = internalDataModel()
        } catch (mte: MirroredTypeException) {
            val typeUtils = processingEnv.typeUtils
            return if (mte.typeMirror.kind.isPrimitive) {
                typeUtils.boxedClass(mte.typeMirror as PrimitiveType)
            } else {
                typeUtils.asElement(mte.typeMirror) as TypeElement
            }
        }
        throw Exception("Shouldn't have reached here!")
    }
}