package annotation.wrappers.annotations.datamodel

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

abstract class DataModelAnnotation {

    protected abstract fun internalDataModel(): Class<out Any>

    fun getTypeElement(processingEnv: ProcessingEnvironment): TypeElement {
        try {
            val dataModel = internalDataModel()
        } catch (mte: MirroredTypeException) {
                val typeUtils = processingEnv.typeUtils
                return typeUtils.asElement(mte.typeMirror) as TypeElement
        }
        throw Exception("Shouldn't have reached here!")
    }

}