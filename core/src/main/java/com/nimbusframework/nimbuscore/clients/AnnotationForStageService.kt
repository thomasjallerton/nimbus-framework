package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

class AnnotationForStageService {

    fun <A: Annotation> getAnnotation(dataClazz: Class<*>, annotationClazz: Class<A>, stage: String, stageAccessor: (A) -> Array<String>): A {
        val annotations = dataClazz.getAnnotationsByType(annotationClazz)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (annotation in annotations) {
            if (stageAccessor(annotation).contains(stage)) {
                return annotation
            }
        }
        return annotations.firstOrNull { stageAccessor(it).isEmpty() } ?: throw InvalidStageException()
    }

}
