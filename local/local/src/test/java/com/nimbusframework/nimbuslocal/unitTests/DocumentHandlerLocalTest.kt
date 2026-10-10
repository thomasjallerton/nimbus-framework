package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.Document
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleDocumentHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

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

        insertMethod.timesInvoked shouldBe 1
        removeMethod.timesInvoked shouldBe 1
        modifyMethod.timesInvoked shouldBe 1
    }

    @Test
    fun insertingIntoDatabaseTriggersInsertCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val insertMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleInsert")

        documentStore.put(documentOne)

        insertMethod.mostRecentInvokeArgument shouldBe documentOne
        insertMethod.mostRecentValueReturned shouldBe true
    }

    @Test
    fun removingFromDatabaseTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val removeMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleRemove")

        documentStore.put(documentOne)
        documentStore.delete(documentOne)

        removeMethod.mostRecentInvokeArgument shouldBe documentOne
        removeMethod.mostRecentValueReturned shouldBe true
    }

    @Test
    fun removingFromDatabaseWithKeyTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val removeMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleRemove")

        documentStore.put(documentOne)
        documentStore.deleteKey("testDocument")

        removeMethod.mostRecentInvokeArgument shouldBe documentOne
        removeMethod.mostRecentValueReturned shouldBe true
    }

    @Test
    fun modifyingItemInDatabaseTriggersModifyCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val documentStore = localDeployment.getDocumentStore(Document::class.java)

        val modifyMethod = localDeployment.getMethod(ExampleDocumentHandler::class.java, "handleModify")

        documentStore.put(documentOne)

        val newDocument = documentOne.copy(people = listOf(Person("TestPerson2", 23)))

        documentStore.put(newDocument)

        modifyMethod.mostRecentInvokeArgument shouldBe listOf(documentOne, newDocument)
        modifyMethod.mostRecentValueReturned shouldBe true
    }
}
