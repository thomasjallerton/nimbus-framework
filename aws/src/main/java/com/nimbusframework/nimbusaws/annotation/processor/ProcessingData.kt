package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbuscore.persisted.NimbusState

data class ProcessingData(
    val nimbusState: NimbusState,
    // qualified names of classes needed for reflection
    val classesForReflection: MutableSet<String> = mutableSetOf()
)
