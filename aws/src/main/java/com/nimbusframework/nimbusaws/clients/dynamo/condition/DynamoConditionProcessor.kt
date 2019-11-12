package com.nimbusframework.nimbusaws.clients.dynamo.condition

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.store.conditions.BracketsCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanOperator
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
            is BooleanComparisonCondition -> processBooleanComparisonCondition(condition, valueMap)
            is BracketsCondition -> "( ${processCondition(condition.condition, valueMap)} )"
            is FunctionCondition -> processFunctionCondition(condition)
            else -> throw UnsupportedOperationException("${condition.javaClass.simpleName} is not supported")
        }
    }

    private fun processComparisonCondition(condition: ComparisionCondition, valueMap: MutableMap<String, AttributeValue>): String {
        val operator = when (condition.operator) {
            ComparisonOperator.GREATER_THAN -> ">"
            ComparisonOperator.EQUAL -> "="
            ComparisonOperator.NOT_EQUAL -> "<>"
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> ">="
            ComparisonOperator.LESS_THAN -> "<"
            ComparisonOperator.LESS_THAN_OR_EQUAL -> "<="
        }
        return processConditionVariable(condition.value1, valueMap) + " " + operator + " " + processConditionVariable(condition.value2, valueMap)
    }

    private fun processConditionVariable(conditionVariable: ConditionVariable, valueMap: MutableMap<String, AttributeValue>): String {
        return when (conditionVariable) {
            is ColumnVariable -> {
                dynamoClient.getColumnName(conditionVariable.fieldName)
            }
            else -> {
                val variable = ":variable${valueMap.size}"
                valueMap[variable] = dynamoClient.toAttributeValue(conditionVariable.getValue())
                variable
            }
        }
    }

    private fun processBooleanComparisonCondition(condition: BooleanComparisonCondition, valueMap: MutableMap<String, AttributeValue>): String {
        val operator = when (condition.operator) {
            BooleanOperator.AND -> "AND"
            BooleanOperator.OR -> "OR"
        }
        return processCondition(condition.value1, valueMap) + " " + operator + " " + processCondition(condition.value2, valueMap)
    }

    private fun processFunctionCondition(functionCondition: FunctionCondition): String {
        return when (functionCondition) {
            is AttributeExists -> "attribute_exists ( ${dynamoClient.getColumnName(functionCondition.fieldName)} )"
            is AttributeNotExists -> "attribute_not_exists ( ${dynamoClient.getColumnName(functionCondition.fieldName)} )"
            else -> throw UnsupportedOperationException("${functionCondition.javaClass.simpleName} is not supported")
        }
    }
}