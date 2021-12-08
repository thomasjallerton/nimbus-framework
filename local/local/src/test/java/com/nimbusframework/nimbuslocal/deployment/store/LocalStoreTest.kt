package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import com.nimbusframework.nimbuslocal.exampleModels.StoreItem
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.maps.shouldContainExactly

internal class LocalStoreTest : AnnotationSpec() {

    private val storeItem = StoreItem()
    private val storeItem2 = StoreItem("newItem")

    private lateinit var localStore: LocalStore<String, StoreItem>

    @BeforeEach
    fun setup() {
        localStore = LocalStore(String::class.java, StoreItem::class.java, "string", StoreItem.allAttributes)
    }

    @Test
    fun addIncreasesSizeAndAddsCorrectItem() {
        localStore.size() shouldBe 0

        localStore.put("key", storeItem)

        localStore.size() shouldBe 1
        localStore.get("key") shouldBe storeItem
    }

    @Test
    fun deleteRemovesItem() {
        localStore.put("key", storeItem)
        localStore.delete("key")
        localStore.size() shouldBe 0
    }

    @Test
    fun deleteRemovesCorrectItem() {
        localStore.put("key", storeItem)
        localStore.put("key2", storeItem)
        localStore.delete("key")
        localStore.size() shouldBe 1
        localStore.get("key") shouldBe null
        localStore.get("key2") shouldBe storeItem
    }

    @Test
    fun getReturnsCorrectItem() {
        localStore.put("key", storeItem)
        localStore.put("key2", storeItem2)

        localStore.get("key") shouldBe storeItem
        localStore.get("key2") shouldBe storeItem2
    }

    @Test
    fun getAllReturnsCorrectValues() {
        localStore.put("key", storeItem)
        localStore.put("key2", storeItem2)

        val getAllResults = localStore.getAll() shouldContainExactly mapOf(Pair("key", storeItem), Pair("key2", storeItem2))
    }

    @Test(expected = StoreConditionException::class)
    fun correctlyThrowsConditionExceptionOnPut() {
        localStore.put("key", storeItem, AttributeExists("name"))
    }

    @Test(expected = StoreConditionException::class)
    fun correctlyThrowsConditionExceptionOnDelete() {
        localStore.delete("key", AttributeExists("name"))

    }

    @Test
    fun transactionalGetWorksAsExpected() {
        localStore.put("key", storeItem)

        val request = localStore.getReadItem("key") as ReadItemRequestLocal
        request.executeRead() shouldBe storeItem
    }

    @Test
    fun transactionalPutWorksAsExpected() {
        val request = localStore.getWriteItem("key", storeItem) as WriteItemRequestLocal
        request.executeWrite()
        localStore.get("key") shouldBe storeItem
    }

    @Test
    fun transactionalDeleteWorksAsExpected() {
        localStore.put("key", storeItem)

        val request = localStore.getDeleteKeyItemRequest("key") as WriteItemRequestLocal
        request.executeWrite()
        localStore.get("key") shouldBe null
    }

    @Test
    fun transactionalIncrementWorksAsExpected() {
        localStore.put("key", storeItem)
        val requests: MutableList<WriteItemRequestLocal> = mutableListOf()
        requests.add(localStore.getIncrementValueRequest("key", "int", 10) as WriteItemRequestLocal)
        requests.add(localStore.getIncrementValueRequest("key", "long", 10) as WriteItemRequestLocal)
        requests.add(localStore.getIncrementValueRequest("key", "short", 10) as WriteItemRequestLocal)
        requests.add(localStore.getIncrementValueRequest("key", "float", 10) as WriteItemRequestLocal)
        requests.add(localStore.getIncrementValueRequest("key", "double", 10) as WriteItemRequestLocal)

        requests.forEach { it.executeWrite() }

        val item = localStore.get("key")!!
        item.int shouldBe 10
        item.long shouldBe 10
        item.short shouldBe 10.toShort()
        item.float shouldBe 10f
        item.double shouldBe 10.0
    }

    @Test
    fun transactionalDecrementWorksAsExpected() {
        localStore.put("key", storeItem)
        val requests: MutableList<WriteItemRequestLocal> = mutableListOf()
        requests.add(localStore.getDecrementValueRequest("key", "int", 10) as WriteItemRequestLocal)
        requests.add(localStore.getDecrementValueRequest("key", "long", 10) as WriteItemRequestLocal)
        requests.add(localStore.getDecrementValueRequest("key", "short", 10) as WriteItemRequestLocal)
        requests.add(localStore.getDecrementValueRequest("key", "float", 10) as WriteItemRequestLocal)
        requests.add(localStore.getDecrementValueRequest("key", "double", 10) as WriteItemRequestLocal)

        requests.forEach { it.executeWrite() }

        val item = localStore.get("key")!!
        item.int shouldBe -10
        item.long shouldBe -10
        item.short shouldBe (-10).toShort()
        item.float shouldBe -10f
        item.double shouldBe -10.0
    }
}