package com.nimbusframework.nimbuscore.annotations

import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

object AnnotationHelper {

    fun <T: Annotation> getAnnotationForStage(clazz: Class<*>, annotation: KClass<T>, stage: String, getStages: (T) -> Array<String>): T? {
        val allAnnotations = clazz.getAnnotationsByType(annotation.java)
        return allAnnotations.firstOrNull { getStages(it).contains(stage) }
            ?: allAnnotations.firstOrNull { getStages(it).isEmpty() }
    }

    fun <T: Annotation> getAnnotationForStage(clazz: TypeElement, annotation: KClass<T>, stage: String, getStages: (T) -> Array<String>): T? {
        val allAnnotations = clazz.getAnnotationsByType(annotation.java)
        return allAnnotations.firstOrNull { getStages(it).contains(stage) }
            ?: allAnnotations.firstOrNull { getStages(it).isEmpty() }
    }

}
