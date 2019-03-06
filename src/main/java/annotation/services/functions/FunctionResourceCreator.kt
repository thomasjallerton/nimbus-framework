package annotation.services.functions

import persisted.NimbusState
import cloudformation.processing.MethodInformation
import cloudformation.resource.ResourceCollection
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import annotation.services.ResourceFinder
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.type.ExecutableType

abstract class FunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        protected val processingEnv: ProcessingEnvironment
) {

    protected val resourceFinder: ResourceFinder = ResourceFinder(updateResources, processingEnv, nimbusState)
    protected val messager = processingEnv.messager

    abstract fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation>

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