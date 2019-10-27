package com.nimbusframework.nimbuscore.persisted

data class HandlerInformation(
        val handlerClassPath: String = "",
        val handlerFile: String = "",
        val usesClients: MutableSet<ClientType> = mutableSetOf(),
        val extraDependencies: MutableSet<String> = mutableSetOf(),
        val replacementVariable: String = "",
        val stages: Set<String> = setOf()
)