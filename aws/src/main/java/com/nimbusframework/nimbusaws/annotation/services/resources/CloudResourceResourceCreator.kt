package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

abstract class CloudResourceResourceCreator(
        private val roundEnvironment: RoundEnvironment,
        protected val cfDocuments: MutableMap<String, CloudFormationFiles>,
        private val singleClass: Class<out Annotation>,
        private val repeatableClass: Class<out Annotation>
) {

    fun create() {
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(singleClass)
        val annotatedElementsRepeatable = roundEnvironment.getElementsAnnotatedWith(repeatableClass)

        annotatedElements.forEach { type -> handleType(type) }
        annotatedElementsRepeatable.forEach { type -> handleType(type) }
    }

    abstract fun handleType(type: Element)

    protected fun determineTableName(givenName: String, className: String, stage: String): String {
        return if (givenName == "") {
            "$className$stage"
        } else {
            "$givenName$stage"
        }
    }
}