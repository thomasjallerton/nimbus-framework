package com.nimbusframework.nimbuscore.clients.store.conditions

import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.BooleanOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.NotCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ColumnVariable
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.NumericVariable
import io.kotlintest.specs.AnnotationSpec
import io.kotlintest.shouldBe

class ConditionBuilderTest : AnnotationSpec() {

    @Test
    fun canProcessFunction() {
        ConditionBuilder.ifFunction(AttributeExists("test")).build() shouldBe AttributeExists("test")
    }

    @Test
    fun canProcessComparison() {
        ConditionBuilder.ifComparison("field", ComparisonOperator.EQUAL, NumericVariable(20)).build() shouldBe ComparisionCondition(ColumnVariable("field"), ComparisonOperator.EQUAL, NumericVariable(20))
    }

    @Test
    fun canProcessAndCondition() {
        ConditionBuilder.ifFunction(AttributeExists("test")).and(AttributeExists("test")).build() shouldBe BooleanComparisonCondition(AttributeExists("test"), BooleanOperator.AND, AttributeExists("test"))
    }

    @Test
    fun canProcessAndConditionWithComparison() {
        ConditionBuilder.ifFunction(AttributeExists("test")).and("field", ComparisonOperator.LESS_THAN, NumericVariable(20)).build() shouldBe BooleanComparisonCondition(AttributeExists("test"), BooleanOperator.AND, ComparisionCondition(ColumnVariable("field"), ComparisonOperator.LESS_THAN, NumericVariable(20)))
    }

    @Test
    fun canProcessOrCondition() {
        ConditionBuilder.ifFunction(AttributeExists("test")).or(AttributeExists("test")).build() shouldBe BooleanComparisonCondition(AttributeExists("test"), BooleanOperator.OR, AttributeExists("test"))
    }

    @Test
    fun canProcessOrConditionWithComparison() {
        ConditionBuilder.ifFunction(AttributeExists("test")).or("field", ComparisonOperator.LESS_THAN, NumericVariable(20)).build() shouldBe BooleanComparisonCondition(AttributeExists("test"), BooleanOperator.OR, ComparisionCondition(ColumnVariable("field"), ComparisonOperator.LESS_THAN, NumericVariable(20)))
    }

    @Test
    fun canProcessNotCondition() {
        ConditionBuilder.ifFunction(AttributeExists("test")).not().build() shouldBe NotCondition(AttributeExists("test"))
    }

    @Test
    fun canProcessInBracesCondition() {
        ConditionBuilder.ifFunction(AttributeExists("test")).inBraces().build() shouldBe BracketsCondition(AttributeExists("test"))
    }

}