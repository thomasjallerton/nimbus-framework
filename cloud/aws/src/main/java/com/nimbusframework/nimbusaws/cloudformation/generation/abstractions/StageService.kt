package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions


class StageService(
        private val defaultStages: List<String>
) {

    fun determineStages(annotationStages: Array<String>): Set<String> {
        return if (annotationStages.isNotEmpty()) {
            annotationStages.toSet()
        } else {
            defaultStages.toSet()
        }
    }
}
