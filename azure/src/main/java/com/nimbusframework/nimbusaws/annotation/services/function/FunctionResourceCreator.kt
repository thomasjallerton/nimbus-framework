package com.nimbusframework.nimbusaws.annotation.services.function

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.arm.ArmFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

abstract class FunctionResourceCreator(
        protected val armFileStages: MutableMap<String, ArmFiles>,
        protected val nimbusState: NimbusState,
        private val singleClass: Class<out Annotation>,
        private val repeatableClass: Class<out Annotation>
) {

    fun handle(roundEnv: RoundEnvironment): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(singleClass)
        val annotatedElementsRepeatable = roundEnv.getElementsAnnotatedWith(repeatableClass)
        val results = LinkedList<FunctionInformation>()

        annotatedElements.forEach { type -> handleElement(type, results) }
        annotatedElementsRepeatable.forEach { type -> handleElement(type, results) }

        afterAllElements()

        return results
    }

    abstract fun handleElement(type: Element, results: MutableList<FunctionInformation>)

    open fun afterAllElements() {}
}