package com.nimbusframework.nimbuscore.clients.store.conditions.variable

data class StringVariable(private val stringVal: String): ConditionVariable() {

    override fun getValue(): Any {
        return stringVal
    }

}