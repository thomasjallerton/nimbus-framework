package localDeployment.unitTests

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import localDeployment.exampleModels.Document
import localDeployment.exampleModels.Person
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentStoreClientLocalTest {

    private val documentOne = Document("testDocument", listOf(Person("TestPerson", 22)))
    private val documentTwo = Document("testDocument2", listOf(Person("TestPerson2", 21)))

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