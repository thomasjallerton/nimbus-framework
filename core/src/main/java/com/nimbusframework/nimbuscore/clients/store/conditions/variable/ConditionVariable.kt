package com.nimbusframework.nimbuscore.clients.store.conditions.variable

import com.nimbusframework.nimbuscore.clients.store.conditions.Condition

abstract class ConditionVariable: Condition {

    abstract fun getValue(): Any

}