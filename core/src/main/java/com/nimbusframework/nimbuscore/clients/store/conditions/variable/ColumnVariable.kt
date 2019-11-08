package com.nimbusframework.nimbuscore.clients.store.conditions.variable

class ColumnVariable(val columnName: String): ConditionVariable() {

    override fun getValue(): Any {
        return columnName
    }

}