package com.nimbusframework.nimbuscore.clients.store.conditions

import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.NotCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.FunctionCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ColumnVariable
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable

class ConditionBuilder private constructor(private var currentCondition: Condition) {

    fun and(condition: Condition): ConditionBuilder {
        currentCondition = BooleanComparisonCondition(currentCondition, BooleanOperator.AND, condition)
        return this
    }

    fun and(fieldName: String, comparisonOperator: ComparisonOperator, conditionVariable: ConditionVariable): ConditionBuilder {
        currentCondition = BooleanComparisonCondition(
                currentCondition,
                BooleanOperator.AND,
                ComparisionCondition(ColumnVariable(fieldName), comparisonOperator, conditionVariable))
        return this
    }

    fun or(condition: Condition): ConditionBuilder {
        currentCondition = BooleanComparisonCondition(currentCondition, BooleanOperator.OR, condition)
        return this
    }

    fun or(fieldName: String, comparisonOperator: ComparisonOperator, conditionVariable: ConditionVariable): ConditionBuilder {
        currentCondition = BooleanComparisonCondition(
                currentCondition,
                BooleanOperator.OR,
                ComparisionCondition(ColumnVariable(fieldName), comparisonOperator, conditionVariable))
        return this
    }


    fun not(): ConditionBuilder {
        currentCondition = NotCondition(currentCondition)
        return this
    }

    fun inBraces(): ConditionBuilder {
        currentCondition = BracketsCondition(currentCondition)
        return this
    }

    fun build(): Condition {
        return currentCondition
    }

    companion object {
        @JvmStatic
        fun ifFunction(condition: FunctionCondition): ConditionBuilder  {
            return ConditionBuilder(condition)
        }

        @JvmStatic
        fun ifComparison(fieldName: String, comparisonOperator: ComparisonOperator, conditionVariable: ConditionVariable): ConditionBuilder  {
            return ConditionBuilder(ComparisionCondition(ColumnVariable(fieldName), comparisonOperator, conditionVariable))
        }
    }
}