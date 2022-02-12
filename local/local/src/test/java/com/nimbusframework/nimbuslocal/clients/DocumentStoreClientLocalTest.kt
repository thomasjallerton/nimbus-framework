package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Document
import com.nimbusframework.nimbuslocal.exampleModels.Person
import io.kotest.core.spec.style.AnnotationSpec
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentStoreClientLocalTest: AnnotationSpec() {

    private val documentOne = Document("testDocument", listOf(Person("TestPerson", 22)), 78)
    private val documentTwo = Document("testDocument2", listOf(Person("TestPerson2", 21)), 98)

    @Test
    fun testAddIncreasesSizeAndAddsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        assertEquals(0, documentStore.size())

        documentStoreClient.put(documentOne)

        assertEquals(1, documentStore.size())
        assertEquals(documentOne, documentStoreClient.get("testDocument")!!)
    }

    @Test
    fun testDeleteKeyRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)

        assertEquals(1, documentStore.size())

        documentStoreClient.deleteKey("testDocument")

        assertEquals(0, documentStore.size())
    }

    @Test
    fun testDeleteObjRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)

        assertEquals(1, documentStore.size())

        documentStoreClient.delete(documentOne)

        assertEquals(0, documentStore.size())
    }

    @Test
    fun testDeleteObjRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)
        documentStoreClient.delete(documentOne)

        assertEquals(1, documentStore.size())
        assertEquals(documentTwo, documentStoreClient.get("testDocument2"))
    }

    @Test
    fun testDeleteKeyRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStore = localDeployment.getDocumentStore(Document::class.java)
        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)
        documentStoreClient.deleteKey("testDocument")

        assertEquals(1, documentStore.size())
        assertEquals(documentTwo, documentStoreClient.get("testDocument2"))
    }

    @Test
    fun testGetReturnsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)

        assertEquals(documentTwo, documentStoreClient.get("testDocument2"))
    }

    @Test
    fun getAllReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Document::class.java)

        val documentStoreClient = localDeployment.getDocumentStore(Document::class.java)

        documentStoreClient.put(documentOne)
        documentStoreClient.put(documentTwo)

        val getAllResults = documentStoreClient.getAll()
        assertEquals(2, getAllResults.size)
        assertTrue(getAllResults.contains(documentOne))
        assertTrue(getAllResults.contains(documentTwo))
    }
}