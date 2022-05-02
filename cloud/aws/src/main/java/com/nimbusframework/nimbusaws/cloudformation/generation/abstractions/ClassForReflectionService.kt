package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import java.lang.reflect.ParameterizedType
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

class ClassForReflectionService(
    private val processingData: ProcessingData,
    private val typeUtils: Types
) {

    private val seenBefore: MutableSet<String> = mutableSetOf()

    private fun isPrimitive(clazz: Class<*>): Boolean {
        return clazz.isPrimitive
    }

    private fun isPrimitive(typeMirror: TypeMirror): Boolean {
        return typeMirror.kind.isPrimitive
    }

    fun addClassForReflection(clazz: Class<*>) {
        if (processingData.nimbusState.customRuntime) {
            internalAddClassForReflection(clazz)
        }
    }

    fun addClassForReflection(typeMirror: TypeMirror?) {
        if (typeMirror != null && processingData.nimbusState.customRuntime) {
            internalAddClassForReflection(typeMirror)
        }
    }

    private fun internalAddClassForReflection(clazz: Class<*>) {
        if (isPrimitive(clazz) || seenBefore.contains(clazz.name)) {
            return
        }
        processingData.addClassForReflection(clazz.name)
        seenBefore.add(clazz.name)
        clazz.declaredFields.forEach {
            internalAddClassForReflection(it.type)
            val genericType = it.genericType
            if (genericType is ParameterizedType) {
                genericType.actualTypeArguments.forEach { type ->
                    if (type is Class<*>) {
                        internalAddClassForReflection(type)
                    }
                }
            }
        }
    }

    private fun internalAddClassForReflection(typeMirror: TypeMirror) {
        if (isPrimitive(typeMirror) || seenBefore.contains(typeMirror.toString())) {
            return
        }
        seenBefore.add(typeMirror.toString())
        val element = typeUtils.asElement(typeMirror)
        if (element != null) {
            if (element is TypeElement) {
                processingData.addClassForReflection(determineFlatName(element))
            }
            element.enclosedElements?.forEach {
                if (it.kind.isField) {
                    internalAddClassForReflection(it.asType())
                }
            }
        }

        if (typeMirror is DeclaredType) {
            typeMirror.typeArguments.forEach { internalAddClassForReflection(it) }
        }
    }

    private fun determineFlatName(element: Element): String {
        val nestedElements = mutableListOf<String>()
        var currentElem = element
        while (currentElem is TypeElement && currentElem.nestingKind.isNested) {
            nestedElements.add(0, currentElem.simpleName.toString())
            currentElem = currentElem.enclosingElement
        }
        if (nestedElements.isEmpty()) {
            return currentElem.toString()
        }
        return currentElem.toString() + "$" + nestedElements.joinToString("$")
    }

}
