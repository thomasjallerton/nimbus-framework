package com.nimbusframework.nimbuscore.persisted

data class HandlerInformation(
        val handlerClassPath: String = "",
        val handlerFile: String = "",
        val extraDependencies: MutableSet<String> = mutableSetOf(),
        val replacementVariable: String = "",
        val stages: Set<String> = setOf()
)
