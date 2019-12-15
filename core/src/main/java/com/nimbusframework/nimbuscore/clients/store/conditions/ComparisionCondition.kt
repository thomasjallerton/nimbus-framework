package com.nimbusframework.nimbuscore.clients.store.conditions

import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable

data class ComparisionCondition(val value1: ConditionVariable, val operator: ComparisonOperator, val value2: ConditionVariable): Condition