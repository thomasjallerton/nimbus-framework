package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.store.conditions.function.AttributeExists
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.deployment.store.LocalStore
import com.nimbusframework.nimbuslocal.exampleModels.StoreItem
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

internal class TransactionalClientLocalTest: AnnotationSpec() {

    //Needed to instantiate the ClientBuilder
    private val localNimbusDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal.exampleModels")

    private val storeItem = StoreItem()
    private val storeItem2 = StoreItem("newItem")

    private val underTest = ClientBuilder.getTransactionalClient()

    private lateinit var localStore: LocalStore<String, StoreItem>

    @BeforeEach
    fun setup() {
        localStore = LocalStore(String::class.java, StoreItem::class.java, "string", StoreItem.allAttributes)
    }

    @Test
    fun canCorrectlyReadItemsFromDocumentStore() {
        localStore.put("key", storeItem)
        localStore.put("key2", storeItem2)
        val requests = listOf(localStore.getReadItem("key"), localStore.getReadItem("key2"))
        val result = underTest.executeReadTransaction(requests)
        result.size shouldBe 2
        result[0] shouldBe storeItem
        result[1] shouldBe storeItem2
    }

    @Test
    fun canCorrectlyWriteItemsToDocumentStore() {
        val requests = listOf(localStore.getWriteItem("key", storeItem), localStore.getWriteItem("key2", storeItem2))
        underTest.executeWriteTransaction(requests)
        localStore.size() shouldBe 2
        localStore.get("key") shouldBe storeItem
        localStore.get("key2") shouldBe storeItem2
    }

    @Test
    fun correctlyRollsBackWriteTransactionsErrors() {
        val requests = listOf(localStore.getWriteItem("key", storeItem), localStore.getWriteItem("key2", storeItem2, AttributeExists("key2")))
        try {
            underTest.executeWriteTransaction(requests)
        } catch (e: StoreConditionException) {
            // Catch exception that will be thrown
        }
        localStore.size() shouldBe 0
    }

    @Test
    fun correctlyRollsBackWriteTransactionsErrorsWithExistingItems() {
        localStore.put("original", storeItem)
        val requests = listOf(localStore.getWriteItem("key", storeItem), localStore.getWriteItem("key2", storeItem2, AttributeExists("key2")))
        try {
            underTest.executeWriteTransaction(requests)
        } catch (e: StoreConditionException) {
            // Catch exception that will be thrown
        }
        localStore.size() shouldBe 1
        localStore.get("original") shouldBe storeItem
    }

    @Test(expected = StoreConditionException::class)
    fun correctlyThrowsStoreConditionException() {
        val requests = listOf(localStore.getWriteItem("key", storeItem), localStore.getWriteItem("key2", storeItem2, AttributeExists("key2")))
        underTest.executeWriteTransaction(requests)
    }
}