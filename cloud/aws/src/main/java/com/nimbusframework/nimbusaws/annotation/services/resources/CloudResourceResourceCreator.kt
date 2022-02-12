package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.annotation.services.StageService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

abstract class CloudResourceResourceCreator(
        private val roundEnvironment: RoundEnvironment,
        protected val cfDocuments: MutableMap<String, CloudFormationFiles>,
        protected val nimbusState: NimbusState,
        private val singleAgnosticClass: Class<out Annotation>,
        private val repeatableAgnosticClass: Class<out Annotation>?,
        private val singleSpecificClass: Class<out Annotation>? = null,
        private val repeatableSpecificClass: Class<out Annotation>? = null
) {

    protected val stageService = StageService(nimbusState.defaultStages)

    fun create() {
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(singleAgnosticClass)
        annotatedElements.forEach { type -> handleAgnosticType(type) }

        if (repeatableAgnosticClass != null) {
            val annotatedElementsRepeatable = roundEnvironment.getElementsAnnotatedWith(repeatableAgnosticClass)
            annotatedElementsRepeatable.forEach { type -> handleAgnosticType(type) }
        }
        if (singleSpecificClass != null) {
            val specificAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(singleSpecificClass)
            specificAnnotatedElements.forEach { type -> handleSpecificType(type) }
        }
        if (repeatableSpecificClass != null) {
            val repeatableAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(repeatableSpecificClass)
            repeatableAnnotatedElements.forEach { type -> handleSpecificType(type) }
        }
    }

    abstract fun handleAgnosticType(type: Element)

    open fun handleSpecificType(type: Element) {}

    protected fun determineTableName(givenName: String, className: String, stage: String): String {
        return if (givenName == "") {
            "$className$stage"
        } else {
            "$givenName$stage"
        }
    }
}
