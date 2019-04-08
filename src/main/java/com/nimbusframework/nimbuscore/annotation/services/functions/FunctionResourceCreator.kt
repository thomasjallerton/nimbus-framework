package com.nimbusframework.nimbuscore.annotation.services.functions

import com.nimbusframework.nimbuscore.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbuscore.annotation.services.ResourceFinder
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.type.ExecutableType

abstract class FunctionResourceCreator(
        protected val cfDocuments: MutableMap<String, CloudFormationDocuments>,
        nimbusState: NimbusState,
        protected val processingEnv: ProcessingEnvironment,
        private val singleClass: Class<out Annotation>,
        private val repeatableClass: Class<out Annotation>
) {

    protected val resourceFinder: ResourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)
    protected val messager = processingEnv.messager

    fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(singleClass)
        val annotatedElementsRepeatable = roundEnv.getElementsAnnotatedWith(repeatableClass)
        val results = LinkedList<FunctionInformation>()

        annotatedElements.forEach { type -> handleElement(type, functionEnvironmentService, results) }
        annotatedElementsRepeatable.forEach { type -> handleElement(type, functionEnvironmentService, results) }

        return results
    }

    abstract fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService, results: MutableList<FunctionInformation>)

    protected fun extractMethodInformation(type: Element): MethodInformation {
        val methodName = type.simpleName.toString()
        val enclosing = type.enclosingElement
        val className = enclosing.simpleName.toString()

        val executableType = type.asType() as ExecutableType
        val parameters = executableType.parameterTypes
        val returnType = executableType.returnType

        val packageElem = enclosing.enclosingElement as PackageElement
        val qualifiedName = packageElem.qualifiedName.toString()

        return MethodInformation(className, methodName, qualifiedName, parameters, returnType)
    }
}