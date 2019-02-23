package localDeployment

import localDeployment.exampleModels.KeyValue
import localDeployment.exampleModels.Person
import localDeployment.exampleHandlers.ExampleKeyValueHandler
import org.junit.jupiter.api.Test
import testing.LocalNimbusDeployment
import kotlin.test.assertEquals

class KeyValueHandlerLocalTest {

    private val keyValueItem = KeyValue("keyvalueitem", listOf(Person("TestPerson", 22)))

    @Test
    fun insertingIntoDatabaseTriggersAllFunctions() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("localDeployment")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")
        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")
        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        assertEquals(1, insertMethod.timesInvoked)
        assertEquals(1, removeMethod.timesInvoked)
        assertEquals(1, modifyMethod.timesInvoked)
    }

    @Test
    fun insertingIntoDatabaseTriggersInsertCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("localDeployment")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val insertMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleInsert")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        assertEquals(keyValueItem, insertMethod.mostRecentInvokeArgument)
        assertEquals(true, insertMethod.mostRecentValueReturned)
    }

    @Test
    fun removingFromDatabaseTriggersRemoveCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("localDeployment")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val removeMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleRemove")

        keyValueStore.put("KEYVALUE1", keyValueItem)
        keyValueStore.delete("KEYVALUE1")

        assertEquals(keyValueItem, removeMethod.mostRecentInvokeArgument)
        assertEquals(true, removeMethod.mostRecentValueReturned)
    }


    @Test
    fun modifyingItemInDatabaseTriggersModifyCorrectly() {
        val localDeployment = LocalNimbusDeployment.getNewInstance("localDeployment")
        val keyValueStore = localDeployment.getKeyValueStore<String, KeyValue>(KeyValue::class.java)

        val modifyMethod = localDeployment.getMethod(ExampleKeyValueHandler::class.java, "handleModify")

        keyValueStore.put("KEYVALUE1", keyValueItem)

        val newDocument = keyValueItem.copy(people = listOf(Person("TestPerson2", 23)))

        keyValueStore.put("KEYVALUE1", newDocument)

        assertEquals(listOf(keyValueItem, newDocument), modifyMethod.mostRecentInvokeArgument)
        assertEquals(true, modifyMethod.mostRecentValueReturned)
    }
}