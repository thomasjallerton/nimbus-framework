package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

abstract class CloudResourceResourceCreator(
        private val roundEnvironment: RoundEnvironment,
        protected val cfDocuments: MutableMap<String, CloudFormationFiles>,
        private val singleAgnosticClass: Class<out Annotation>,
        private val repeatableAgnosticClass: Class<out Annotation>,
        private val singleSpecificClass: Class<out Annotation>? = null,
        private val repeatableSpecificClass: Class<out Annotation>? = null
) {

    fun create() {
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(singleAgnosticClass)
        val annotatedElementsRepeatable = roundEnvironment.getElementsAnnotatedWith(repeatableAgnosticClass)

        annotatedElements.forEach { type -> handleAgnosticType(type) }
        annotatedElementsRepeatable.forEach { type -> handleAgnosticType(type) }

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