package com.nimbusframework.nimbuscore.clients.store

data class UpdateCondition(val numericFieldName: String, val updateCondition: ConditionOperator, val amount: Float)