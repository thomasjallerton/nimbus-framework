package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleKeyValueHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class KeyValueHandlerLocalTest: AnnotationSpec() {

    private val keyValueItem = KeyValue("keyvalueitem", listOf(Person("TestPerson", 22)))

    @Test
    fun insertingIntoDatabaseTriggersAllFunctions() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")
        insertMethod.timesInvoked shouldBe 0
        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")
        removeMethod.timesInvoked shouldBe 0
        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")
        modifyMethod.timesInvoked shouldBe 0


        keyValueStore.put("KEYVALUE1", keyValueItem)

        insertMethod.timesInvoked shouldBe 1
        removeMethod.timesInvoked shouldBe 1
        modifyMethod.timesInvoked shouldBe 1
    }

    @Test
    fun insertingIntoDatabaseTriggersInsertCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        insertMethod.mostRecentInvokeArgument shouldBe keyValueItem
        insertMethod.mostRecentValueReturned shouldBe true
    }

    @Test
    fun removingFromDatabaseTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")

        keyValueStore.put("KEYVALUE1", keyValueItem)
        keyValueStore.delete("KEYVALUE1")

        removeMethod.mostRecentInvokeArgument shouldBe keyValueItem
        removeMethod.mostRecentValueReturned shouldBe true
    }


    @Test
    fun modifyingItemInDatabaseTriggersModifyCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        val newDocument = keyValueItem.copy(people = listOf(Person("TestPerson2", 23)))

        keyValueStore.put("KEYVALUE1", newDocument)

        modifyMethod.mostRecentInvokeArgument shouldBe listOf(keyValueItem, newDocument)
        modifyMethod.mostRecentValueReturned shouldBe true
    }
}
