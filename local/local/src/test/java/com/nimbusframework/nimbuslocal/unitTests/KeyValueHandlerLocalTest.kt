package com.nimbusframework.nimbuslocal.unitTests

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleModels.KeyValue
import com.nimbusframework.nimbuslocal.exampleModels.Person
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleKeyValueHandler
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KeyValueHandlerLocalTest: AnnotationSpec() {

    private val keyValueItem = KeyValue("keyvalueitem", listOf(Person("TestPerson", 22)))

    @Test
    fun insertingIntoDatabaseTriggersAllFunctions() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")
        assertEquals(0, insertMethod.timesInvoked)
        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")
        assertEquals(0, removeMethod.timesInvoked)
        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")
        assertEquals(0, modifyMethod.timesInvoked)


        keyValueStore.put("KEYVALUE1", keyValueItem)

        assertEquals(1, insertMethod.timesInvoked)
        assertEquals(1, removeMethod.timesInvoked)
        assertEquals(1, modifyMethod.timesInvoked)
    }

    @Test
    fun insertingIntoDatabaseTriggersInsertCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        assertEquals(keyValueItem, insertMethod.mostRecentInvokeArgument)
        assertEquals(true, insertMethod.mostRecentValueReturned)
    }

    @Test
    fun removingFromDatabaseTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")

        keyValueStore.put("KEYVALUE1", keyValueItem)
        keyValueStore.delete("KEYVALUE1")

        assertEquals(keyValueItem, removeMethod.mostRecentInvokeArgument)
        assertEquals(true, removeMethod.mostRecentValueReturned)
    }


    @Test
    fun modifyingItemInDatabaseTriggersModifyCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("com.nimbusframework.nimbuslocal")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        val newDocument = keyValueItem.copy(people = listOf(Person("TestPerson2", 23)))

        keyValueStore.put("KEYVALUE1", newDocument)

        assertEquals(listOf(keyValueItem, newDocument), modifyMethod.mostRecentInvokeArgument)
        assertEquals(true, modifyMethod.mostRecentValueReturned)
    }
}