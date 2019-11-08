package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable

class StringVariable(private val stringVal: String): ConditionVariable() {

    override fun getValue(): Any {
        return stringVal
    }

}