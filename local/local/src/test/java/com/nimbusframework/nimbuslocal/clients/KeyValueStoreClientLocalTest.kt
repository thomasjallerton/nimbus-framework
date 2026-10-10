package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonCondition
import com.nimbusframework.nimbuscore.clients.store.conditions.ComparisonOperator
import com.nimbusframework.nimbuscore.clients.store.conditions.variable.ConditionVariable
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.streams.toList

class KeyValueStoreClientLocalTest: AnnotationSpec() {

    private val houseOne = KeyValue("testHouse", listOf(Person("TestPerson", 22)))
    private val houseTwo = KeyValue("testHouse2", listOf(Person("TestPerson2", 21)))

    @Test
    fun testAddIncreasesSizeAndAddsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.size() shouldBe 0

        keyValueStoreClient.put(10, houseOne)

        keyValueStore.size() shouldBe 1
        keyValueStore.get(10) shouldBe houseOne
    }

    @Test
    fun testRemoveItemRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStoreClient.delete(10)

        keyValueStore.size() shouldBe 0
    }

    @Test
    fun testRemoveItemRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)
        keyValueStoreClient.delete(10)

        keyValueStore.size() shouldBe 1
        keyValueStore.get(5) shouldBe houseTwo
    }

    @Test
    fun testGetReturnsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        keyValueStoreClient.get(5) shouldBe houseTwo
    }

    @Test
    fun getAllReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        val getAllResults = keyValueStoreClient.getAll()
        getAllResults.size shouldBe 2
        getAllResults[10] shouldBe houseOne
        getAllResults[5] shouldBe houseTwo
    }

    @Test
    fun getAllKeysReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        val getAllResults = keyValueStoreClient.getAllKeys().toList()
        getAllResults shouldContainExactlyInAnyOrder listOf(10, 5)
    }

    @Test
    fun filterReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        val result = keyValueStoreClient.filter(ComparisonCondition(ConditionVariable.column("name"), ComparisonOperator.EQUAL, ConditionVariable.string("testHouse")))
        result shouldContainExactly mapOf(Pair(10, houseOne))
    }
}
