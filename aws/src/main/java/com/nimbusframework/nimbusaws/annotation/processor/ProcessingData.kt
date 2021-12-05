package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbuscore.persisted.NimbusState

data class ProcessingData(
    val nimbusState: NimbusState,
    // qualified names of classes needed for reflection
    val classesForReflection: MutableSet<String> = mutableSetOf()
) {

    fun addClassForReflection(qualifiedName: String) {
        if (qualifiedName == "java.util.Set") {
            classesForReflection.add("java.util.HashSet")
            return
        }
        if (qualifiedName.length == 1 ||
            qualifiedName.startsWith("[") ||
            qualifiedName.startsWith("jdk.internal") ||
            qualifiedName.startsWith("java.") ||
            qualifiedName.startsWith("sun.") ||
            qualifiedName.startsWith("com.oracle.") ||
            qualifiedName.startsWith("kotlin.jvm.") ||
            qualifiedName.endsWith("[]")
        ) {
            return
        }
        classesForReflection.add(qualifiedName.substringBefore('<'))
    }

}
