package com.nimbusframework.nimbuslocal.deployment.services

class StageService(
        val deployingStage: String,
        private val deployingDefaultStage: Boolean,
        private val allowedOrigin: Map<String, String>,
        private val allowedHeaders: Map<String, List<String>>
) {

    fun <T> isResourceDeployedInStage(annotations: Array<T>, getStages: (T) -> Array<String>): Boolean {
        for (annotation in annotations) {
            val stages = getStages(annotation)
            if (stages.contains(deployingStage) || (stages.isEmpty() && deployingDefaultStage)) {
                return true
            }
        }
        return false
    }

    fun <T> annotationForStage(annotations: Array<T>, getStages: (T) -> Array<String>): T? {
        for (annotation in annotations) {
            val stages = getStages(annotation)
            if (stages.contains(deployingStage) || (stages.isEmpty() && deployingDefaultStage)) {
                return annotation
            }
        }
        return null
    }

    fun getDefaultAllowedOrigin(): String {
        return allowedOrigin[deployingStage] ?: ""
    }

    fun getDefaultAllowedHeaders(): Array<String> {
        return allowedHeaders[deployingStage]?.toTypedArray() ?: arrayOf()
    }

}
