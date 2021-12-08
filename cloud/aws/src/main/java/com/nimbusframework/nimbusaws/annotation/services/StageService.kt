package com.nimbusframework.nimbusaws.annotation.services


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