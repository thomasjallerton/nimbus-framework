package com.nimbusframework.nimbuscore.clients.store.conditions.variable

class ObjectVariable(private val objValue: Any): ConditionVariable() {

    override fun getValue(): Any {
        return objValue
    }

}
