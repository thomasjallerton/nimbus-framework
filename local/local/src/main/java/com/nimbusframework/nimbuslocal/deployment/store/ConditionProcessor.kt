package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.store.conditions.BracketsCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonCondition
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

class ConditionProcessor <T> (private val clazz: Class<T>, private val keyColumnName: String) {

    fun processCondition(condition: Condition, obj: T?): Boolean {
        return when (condition) {
            is NotCondition -> !processCondition(condition.condition, obj)
            is ComparisonCondition -> processComparisonCondition(condition, obj)
            is BooleanComparisonCondition -> processBooleanComparisonCondition(condition, obj)
            is BracketsCondition -> processCondition(condition.condition, obj)
            is FunctionCondition -> processFunctionCondition(condition, obj)
            else -> throw UnsupportedOperationException("${condition.javaClass.simpleName} is not supported")
        }
    }

    private fun processComparisonCondition(condition: ComparisonCondition, obj: T?): Boolean {
        val value1 = processConditionVariable(condition.value1, obj)
        val value2 = processConditionVariable(condition.value2, obj)

        return when {
            value1 is Double && value2 is Double -> compareTwoVariables(value1 as Double, condition.operator, value2 as Double)
            value1 is Float && value2 is Float -> compareTwoVariables(value1 as Float, condition.operator, value2 as Float)
            value1 is Long && value2 is Long -> compareTwoVariables(value1 as Long, condition.operator, value2 as Long)
            value1 is Int && value2 is Int -> compareTwoVariables(value1 as Int, condition.operator, value2 as Int)
            value1 is Char && value2 is Char -> compareTwoVariables(value1 as Char, condition.operator, value2 as Char)
            value1 is Short && value2 is Short -> compareTwoVariables(value1 as Short, condition.operator, value2 as Short)
            value1 is Byte && value2 is Byte -> compareTwoVariables(value1 as Byte, condition.operator, value2 as Byte)
            value1 is String && value2 is String -> compareTwoVariables(value1 as String, condition.operator, value2 as String)
            value1 is Boolean && value2 is Boolean -> compareTwoVariables(value1 as Boolean, condition.operator, value2 as Boolean)
            else -> throw TypeMismatchException()
        }
    }

    private fun <C> compareTwoVariables(value1: Comparable<C>, operator: ComparisonOperator, value2: C): Boolean {
        return when (operator) {
            ComparisonOperator.GREATER_THAN -> value1 > value2
            ComparisonOperator.EQUAL -> value1 == value2
            ComparisonOperator.NOT_EQUAL -> value1 != value2
            ComparisonOperator.GREATER_THAN_OR_EQUAL -> value1 >= value2
            ComparisonOperator.LESS_THAN -> value1 < value2
            ComparisonOperator.LESS_THAN_OR_EQUAL -> value1 <= value2
        }
    }

    private fun processConditionVariable(conditionVariable: ConditionVariable, obj: T?): Any {
        return when (conditionVariable) {
            is ColumnVariable -> {
                val field = clazz.getDeclaredField(conditionVariable.fieldName)
                if (obj == null) throw IllegalStateException("Item does not exist during condition check")
                field.isAccessible = true
                field[obj]
            }
            else -> {
                conditionVariable.getValue()
            }
        }
    }

    private fun processBooleanComparisonCondition(condition: BooleanComparisonCondition, obj: T?): Boolean {
        return when (condition.operator) {
            BooleanOperator.AND -> processCondition(condition.value1, obj) && processCondition(condition.value2, obj)
            BooleanOperator.OR  -> processCondition(condition.value1, obj) || processCondition(condition.value2, obj)
        }
    }

    private fun processFunctionCondition(functionCondition: FunctionCondition, obj: T?): Boolean {
        return when (functionCondition) {
            is AttributeExists -> {
                if (obj == null) return false
                if (functionCondition.fieldName == keyColumnName) return true
                val field = clazz.getDeclaredField(functionCondition.fieldName)
                field.isAccessible = true
                field[obj] != null
            }
            is AttributeNotExists -> {
                if (obj == null) return true
                if (functionCondition.fieldName == keyColumnName) return false
                val field = clazz.getDeclaredField(functionCondition.fieldName)
                field.isAccessible = true
                field[obj] == null
            }
            else -> throw UnsupportedOperationException("${functionCondition.javaClass.simpleName} is not supported")
        }
    }
}
