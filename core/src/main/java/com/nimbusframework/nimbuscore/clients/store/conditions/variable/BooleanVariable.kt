package com.nimbusframework.nimbuscore.clients.store.conditions.variable

class BooleanVariable(private val booleanVal: Boolean): ConditionVariable() {

    override fun getValue(): Any {
        return booleanVal
    }

}