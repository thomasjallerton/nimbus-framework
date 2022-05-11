package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.annotations.lambda.CustomLambdaFunctionHandler
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.lambda.handlers.HandlerInformationProvider
import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactory
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.CustomFactoryAnnotation
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.tools.Diagnostic

abstract class FunctionResourceCreator(
    protected val cfDocuments: MutableMap<String, CloudFormationFiles>,
    protected val processingData: ProcessingData,
    protected val processingEnv: ProcessingEnvironment,
    private val decoratorHandlers: Set<FunctionDecoratorHandler>,
    protected val messager: Messager,
    private val singleClass: Class<out Annotation>,
    private val repeatableClass: Class<out Annotation>
) {

    protected val stageService = StageService(processingData.nimbusState.defaultStages)

    protected val nimbusState = processingData.nimbusState

    fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        val annotatedElements = roundEnv.getElementsAnnotatedWithAny(setOf(singleClass, repeatableClass))

        val results = annotatedElements.flatMap { type ->
            val functionInformation = handleElement(type, functionEnvironmentService)
            // handle decorators
            decoratorHandlers.forEach { it.handleDecorator(type, functionInformation) }
            functionInformation
        }

        afterAllElements()

        return results
    }

    abstract fun handleElement(type: Element, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation>

    fun extractMethodInformation(type: Element): FileBuilderMethodInformation {
        return extractMethodInformation(type, processingEnv, messager)
    }

    fun hasEmptyConstructor(element: TypeElement) {
        return hasEmptyConstructor(element, messager)
    }

    open fun afterAllElements() {}

    companion object {

        fun extractMethodInformation(type: Element, processingEnv: ProcessingEnvironment, messager: Messager): FileBuilderMethodInformation {
            val methodName = type.simpleName.toString()
            val enclosing = type.enclosingElement
            val className = enclosing.simpleName.toString()
            val customFactory = extractCustomFactory(enclosing, processingEnv, messager)

            val executableType = type.asType() as ExecutableType
            val parameters = executableType.parameterTypes
            val returnType = executableType.returnType

            val packageElem = enclosing.enclosingElement as PackageElement
            val qualifiedName = packageElem.qualifiedName.toString()

            return FileBuilderMethodInformation(className, customFactory, methodName, qualifiedName, parameters, returnType)
        }

        fun extractReplacementVariable(type: Element): String {
            val methodName = type.simpleName.toString()
            val enclosing = type.enclosingElement
            val packageElem = enclosing.enclosingElement as PackageElement
            val qualifiedName = packageElem.qualifiedName.toString()

            return "\${${qualifiedName.replace(".", "_")}_${enclosing.simpleName}_$methodName}"
        }

        private fun extractCustomFactory(clazz: Element, processingEnv: ProcessingEnvironment, messager: Messager): String? {
            val customFactoryType = (clazz.getAnnotation(CustomFactory::class.java) ?: return null)
            val customFactory = CustomFactoryAnnotation(customFactoryType).getTypeElement(processingEnv)
            if (customFactory.interfaces.size > 1) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "${customFactory.qualifiedName} must only implement CustomFactoryInterface",
                    clazz
                )
            }
            val typeParameter = (customFactory.interfaces.first() as DeclaredType).typeArguments[0]
            if (typeParameter.toString() != clazz.toString()) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Custom factory ${customFactory.qualifiedName} does not implement CustomFactory<${clazz.simpleName}>",
                    clazz
                )
                return null
            }
            hasEmptyConstructor(customFactory, messager)
            return customFactory.qualifiedName.toString()
        }

        protected fun hasEmptyConstructor(element: TypeElement, messager: Messager) {
            val hasEmpty = element.enclosedElements.filter { it.kind == ElementKind.CONSTRUCTOR }
                .map { it.asType() as ExecutableType }
                .any { it.parameterTypes.isEmpty() }

            if (!hasEmpty) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "${element.qualifiedName} must have a non-parameterized constructor",
                    element
                )
            }
        }

        fun createHandlerInformation(type: Element, handlerInformationProvider: HandlerInformationProvider): HandlerInformation {
            val customHandlerAnnotation = type.getAnnotationsByType(CustomLambdaFunctionHandler::class.java)

            if (customHandlerAnnotation.isNotEmpty()) {
                val annotation = customHandlerAnnotation.first()
                return HandlerInformation(
                    "",
                    annotation.handler,
                    extractReplacementVariable(type),
                    annotation.file,
                    annotation.runtime
                )
            }

            return HandlerInformation(
                handlerInformationProvider.getClassFilePath(),
                handlerInformationProvider.getHandler(),
                extractReplacementVariable(type)
            )
        }
    }
}
