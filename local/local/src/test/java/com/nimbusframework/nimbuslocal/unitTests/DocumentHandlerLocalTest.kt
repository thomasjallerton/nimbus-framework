package com.nimbusframework.nimbuslocal.unitTests


import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Document
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleDocumentHandler
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DocumentHandlerLocalTest: AnnotationSpec() {

    private val documentOne = Document("testDocument", listOf(Person("TestPerson", 22)), 67)

    @Test
    fun insertingIntoDatabaseTriggersAllFunctions() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val insertMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleInsert")
        val removeMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleRemove")
        val modifyMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleModify")

        documentStore.put(documentOne)

        assertEquals(1, insertMethod.timesInvoked)
        assertEquals(1, removeMethod.timesInvoked)
        assertEquals(1, modifyMethod.timesInvoked)
    }

    @Test
    fun insertingIntoDatabaseTriggersInsertCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val insertMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleInsert")

        documentStore.put(documentOne)

        assertEquals(documentOne, insertMethod.mostRecentInvokeArgument)
        assertEquals(true, insertMethod.mostRecentValueReturned)
    }

    @Test
    fun removingFromDatabaseTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val removeMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleRemove")

        documentStore.put(documentOne)
        documentStore.delete(documentOne)

        assertEquals(documentOne, removeMethod.mostRecentInvokeArgument)
        assertEquals(true, removeMethod.mostRecentValueReturned)
    }

    @Test
    fun removingFromDatabaseWithKeyTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val removeMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleRemove")

        documentStore.put(documentOne)
        documentStore.deleteKey("testDocument")

        assertEquals(documentOne, removeMethod.mostRecentInvokeArgument)
        assertEquals(true, removeMethod.mostRecentValueReturned)
    }

    @Test
    fun modifyingItemInDatabaseTriggersModifyCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val modifyMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleModify")

        documentStore.put(documentOne)

        val newDocument = documentOne.copy(people = listOf(Person("TestPerson2", 23)))

        documentStore.put(newDocument)

        assertEquals(listOf(documentOne, newDocument), modifyMethod.mostRecentInvokeArgument)
        assertEquals(true, modifyMethod.mostRecentValueReturned)
    }
}