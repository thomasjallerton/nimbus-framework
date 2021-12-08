package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.ConditionBuilder
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeNotExists
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.NumericVariable
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.StringVariable
import com.nimbusframework.nimbuslocal.exampleModels.Document
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.AnnotationSpec

internal class ConditionProcessorTest : AnnotationSpec() {

    private val underTest = ConditionProcessor(Document::class.java, "name")
    private val document = Document("testDocument", null, 78)
    private val keyValue = KeyValue("testKeyValue")

    @Test
    fun canCorrectlyCompareStrings() {
        underTest.processCondition(ConditionBuilder.ifComparison("name", ComparisonOperator.EQUAL, StringVariable("testDocument")).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("name", ComparisonOperator.EQUAL, StringVariable("sdf")).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("name", ComparisonOperator.NOT_EQUAL, StringVariable("askdh")).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("name", ComparisonOperator.NOT_EQUAL, StringVariable("testDocument")).build(), document) shouldBe false
    }

    @Test
    fun canCorrectlyCompareNumbers() {
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.EQUAL, NumericVariable(78)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.EQUAL, NumericVariable(100)).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.NOT_EQUAL, NumericVariable(100)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.NOT_EQUAL, NumericVariable(78)).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.GREATER_THAN, NumericVariable(20)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.GREATER_THAN, NumericVariable(100)).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.GREATER_THAN_OR_EQUAL, NumericVariable(20)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.GREATER_THAN_OR_EQUAL, NumericVariable(79)).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.LESS_THAN, NumericVariable(100)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.LESS_THAN, NumericVariable(20)).build(), document) shouldBe false
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.LESS_THAN_OR_EQUAL, NumericVariable(78)).build(), document) shouldBe true
        underTest.processCondition(ConditionBuilder.ifComparison("number", ComparisonOperator.LESS_THAN_OR_EQUAL, NumericVariable(20)).build(), document) shouldBe false
    }

    @Test
    fun canCorrectlyCallFunctions() {
        underTest.processCondition(AttributeExists("number"), document) shouldBe true
        underTest.processCondition(AttributeExists("people"), document) shouldBe false
        underTest.processCondition(AttributeExists("number"), null) shouldBe false

        underTest.processCondition(AttributeNotExists("number"), document) shouldBe false
        underTest.processCondition(AttributeNotExists("people"), document) shouldBe true
        underTest.processCondition(AttributeNotExists("number"), null) shouldBe true
    }

    @Test
    fun canCorrectlyCallFunctionsKeyValue() {
        val underTest = ConditionProcessor(KeyValue::class.java, "PrimaryKey")
        underTest.processCondition(AttributeExists("PrimaryKey"), keyValue) shouldBe true
        underTest.processCondition(AttributeExists("PrimaryKey"), null) shouldBe false

        underTest.processCondition(AttributeNotExists("PrimaryKey"), keyValue) shouldBe false
        underTest.processCondition(AttributeNotExists("PrimaryKey"), null) shouldBe true
    }

    @Test
    fun canCorrectlyComposeAnd() {
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .and(AttributeExists("people"))
                        .build(),
                document) shouldBe false
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("people"))
                        .and(AttributeExists("number"))
                        .build(),
                document) shouldBe false
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("people"))
                        .and(AttributeExists("people"))
                        .build(),
                document) shouldBe false
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .and(AttributeExists("number"))
                        .build(),
                document) shouldBe true
    }

    @Test
    fun canCorrectlyComposeOr() {
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .or(AttributeExists("people"))
                        .build(),
                document) shouldBe true
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("people"))
                        .or(AttributeExists("number"))
                        .build(),
                document) shouldBe true
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("people"))
                        .or(AttributeExists("people"))
                        .build(),
                document) shouldBe false
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .or(AttributeExists("number"))
                        .build(),
                document) shouldBe true
    }

    @Test
    fun canCorrectlyComposeNot() {
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .not()
                        .build(),
                document) shouldBe false
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("people"))
                        .not()
                        .build(),
                document) shouldBe true
    }

    @Test
    fun canCorrectlyComposeBrackets() {
        underTest.processCondition(
                ConditionBuilder
                        .ifFunction(AttributeExists("number"))
                        .inBraces()
                        .build(),
                document) shouldBe true
    }
}