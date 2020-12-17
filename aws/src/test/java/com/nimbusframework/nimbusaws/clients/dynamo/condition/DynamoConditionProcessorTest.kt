package com.nimbusframework.nimbusaws.clients.dynamo.condition

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient
import com.nimbusframework.nimbuscore.clients.store.conditions.BracketsCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisionCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.ConditionBuilder
import com.nimbusframework.nimbuscore.clients.store.conditions.bool.NotCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeNotExists
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.NumericVariable
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class DynamoConditionProcessorTest : AnnotationSpec() {

    private val dynamoClient: DynamoClient = mockk()
    private val underTest = DynamoConditionProcessor(dynamoClient)

    @Before
    fun setup() {
        every { dynamoClient.getColumnName("test") } returns "TestColumn"
        every { dynamoClient.toAttributeValue(10) } returns AttributeValue("10")
        every { dynamoClient.toAttributeValue(11) } returns AttributeValue("11")
    }

    @Test
    fun canProcessNotCondition() {
        underTest.processCondition(NotCondition(AttributeExists("test")), mutableMapOf()) shouldBe "Not attribute_exists ( TestColumn )"
    }

    @Test
    fun canProcessBracketsCondition() {
        underTest.processCondition(BracketsCondition(AttributeExists("test")), mutableMapOf()) shouldBe "( attribute_exists ( TestColumn ) )"
    }

    @Test
    fun canProcessAttributeExists() {
        underTest.processCondition(AttributeExists("test"), mutableMapOf()) shouldBe "attribute_exists ( TestColumn )"
    }

    @Test
    fun canProcessAttributeNotExists() {
        underTest.processCondition(AttributeNotExists("test"), mutableMapOf()) shouldBe "attribute_not_exists ( TestColumn )"
    }

    @Test
    fun canProcessAndCondition() {
        underTest.processCondition(
                ConditionBuilder.ifFunction(AttributeExists("test"))
                        .and(AttributeExists("test"))
                        .build(), mutableMapOf()) shouldBe "attribute_exists ( TestColumn ) AND attribute_exists ( TestColumn )"
    }

    @Test
    fun canProcessOrCondition() {
        underTest.processCondition(
                ConditionBuilder.ifFunction(AttributeExists("test"))
                        .or(AttributeExists("test"))
                        .build(), mutableMapOf()) shouldBe "attribute_exists ( TestColumn ) OR attribute_exists ( TestColumn )"
    }

    @Test
    fun canProcessGreaterThan() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.GREATER_THAN, NumericVariable(11)),
                valueMap) shouldBe ":variable0 > :variable1"
        valueMap[":variable0"] shouldBe AttributeValue("10")
        valueMap[":variable1"] shouldBe AttributeValue("11")
    }

    @Test
    fun canProcessGreaterThanOrEqualTo() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.GREATER_THAN_OR_EQUAL, NumericVariable(11)),
                valueMap) shouldBe ":variable0 >= :variable1"
        valueMap[":variable0"] shouldBe AttributeValue("10")
        valueMap[":variable1"] shouldBe AttributeValue("11")
    }

    @Test
    fun canProcessLessThan() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.LESS_THAN, NumericVariable(11)),
                valueMap) shouldBe ":variable0 < :variable1"
        valueMap[":variable0"] shouldBe AttributeValue("10")
        valueMap[":variable1"] shouldBe AttributeValue("11")
    }

    @Test
    fun canProcessLessThanOrEqualTo() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.LESS_THAN_OR_EQUAL, NumericVariable(11)),
                valueMap) shouldBe ":variable0 <= :variable1"
    }

    @Test
    fun canProcessEqualTo() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.EQUAL, NumericVariable(11)),
                valueMap) shouldBe ":variable0 = :variable1"
        valueMap[":variable0"] shouldBe AttributeValue("10")
        valueMap[":variable1"] shouldBe AttributeValue("11")
    }

    @Test
    fun canProcessNotEqualTo() {
        val valueMap: MutableMap<String, AttributeValue> = mutableMapOf()
        underTest.processCondition(
                ComparisionCondition(NumericVariable(10), ComparisonOperator.NOT_EQUAL, NumericVariable(11)),
                valueMap) shouldBe ":variable0 <> :variable1"
        valueMap[":variable0"] shouldBe AttributeValue("10")
        valueMap[":variable1"] shouldBe AttributeValue("11")
    }

}