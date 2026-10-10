package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Document
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class DocumentStoreClientLocalTest: AnnotationSpec() {

    private val documentOne = Document("testDocument", listOf(Person("TestPerson", 22)), 78)
    private val documentTwo = Document("testDocument2", listOf(Person("TestPerson2", 21)), 98)

    @Test
    fun testAddIncreasesSizeAndAddsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStore.size() shouldBe 0

        documentStoreClient.put(documentOne)

        documentStore.size() shouldBe 1
        documentStoreClient.get("testDocument") shouldBe documentOne
    }

    @Test
    fun testDeleteKeyRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)

        documentStore.size() shouldBe 1

        documentStoreClient.deleteKey("testDocument")

        documentStore.size() shouldBe 0
    }

    @Test
    fun testDeleteObjRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)

        documentStore.size() shouldBe 1

        documentStoreClient.delete(documentOne)

        documentStore.size() shouldBe 0
    }

    @Test
    fun testDeleteObjRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)
        documentStoreClient.delete(documentOne)

        documentStore.size() shouldBe 1
        documentStoreClient.get("testDocument2") shouldBe documentTwo
    }

    @Test
    fun testDeleteKeyRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)
        documentStoreClient.deleteKey("testDocument")

        documentStore.size() shouldBe 1
        documentStoreClient.get("testDocument2") shouldBe documentTwo
    }

    @Test
    fun testGetReturnsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)

        documentStoreClient.get("testDocument2") shouldBe documentTwo
    }

    @Test
    fun getAllReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)

        val getAllResults = documentStoreClient.getAll().toList()
        getAllResults.size shouldBe 2
        getAllResults shouldContain documentOne
        getAllResults shouldContain documentTwo
    }

    @Test
    fun getAllKeysReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)

        val getAllResults = documentStoreClient.getAllKeys().toList()
        getAllResults shouldContainExactlyInAnyOrder listOf(documentOne.name, documentTwo.name)
    }
}
