package com.nimbusframework.nimbusaws.clients.dynamo.condition

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.store.conditions.BracketsCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.NotCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeNotExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.FunctionCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ColumnVariable
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable

class DynamoConditionProcessor<T>(private val dynamoClient: DynamoClient<T>) {

    fun processCondition(condition: Condition, valueMap: MutableMap<String, AttributeValue>): String {
        return when (condition) {
            is NotCondition -> "Not ${processCondition(condition.condition, valueMap)}"
            is ComparisionCondition -> processComparisonCondition(condition, valueMap)
            is BracketsCondition -> "( ${processCondition(condition.condition, valueMap)} )"
            is FunctionCondition -> processFunctionCondition(condition)
            else -> throw UnsupportedOperationException("${condition.javaClass.simpleName} is not supported")
        }
    }

    private fun processComparisonCondition(condition: ComparisionCondition, valueMap: MutableMap<String, AttributeValue>): String {
        return processConditionVariable(condition.value1, valueMap) + " " + condition.operator.name + " " + processConditionVariable(condition.value2, valueMap)
    }

    private fun processConditionVariable(conditionVariable: ConditionVariable, valueMap: MutableMap<String, AttributeValue>): String {
        return when (conditionVariable) {
            is ColumnVariable -> {
                dynamoClient.getColumnName(conditionVariable.columnName)
            }
            else -> {
                val variable = ":variable${valueMap.size}"
                valueMap[variable] = dynamoClient.toAttributeValue(conditionVariable.getValue())
                variable
            }
        }
    }

    private fun processFunctionCondition(functionCondition: FunctionCondition): String {
        return when (functionCondition) {
            is AttributeExists -> "attribute_exists ( ${dynamoClient.getColumnName(functionCondition.fieldName)} )"
            is AttributeNotExists -> "attribute_not_exists ( ${dynamoClient.getColumnName(functionCondition.fieldName)} )"
            else -> throw UnsupportedOperationException("${functionCondition.javaClass.simpleName} is not supported")
        }
    }
}