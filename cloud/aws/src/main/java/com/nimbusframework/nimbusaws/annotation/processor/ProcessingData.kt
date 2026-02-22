package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbuscore.annotations.function.HttpRequestPartLog
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.persisted.userconfig.HttpErrorMessageType

data class ProcessingData(
    val nimbusState: NimbusState,
    val httpErrorMessageType: HttpErrorMessageType = HttpErrorMessageType.PLAIN_TEXT,
    val httpLogParts: List<HttpRequestPartLog> = listOf(),
    val additionalFunctions: MutableSet<FunctionInformation> = mutableSetOf(),
    // qualified names of classes needed for reflection
    val classesForReflection: MutableSet<String> = mutableSetOf(),
    val defaultRequestHeaders: Map<String, List<String>> = mapOf(),
    val defaultAllowedOrigin: Map<String, String> = mapOf()
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
