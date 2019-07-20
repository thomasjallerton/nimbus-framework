package localDeployment.unitTests

import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import localDeployment.exampleModels.KeyValue
import localDeployment.exampleModels.Person
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KeyValueStoreClientLocalTest {

    private val houseOne = KeyValue("testHouse", listOf(Person("TestPerson", 22)))
    private val houseTwo = KeyValue("testHouse2", listOf(Person("TestPerson2", 21)))

    @Test
    fun testAddIncreasesSizeAndAddsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        assertEquals(0, keyValueStore.size())

        keyValueStoreClient.put(10, houseOne)

        assertEquals(1, keyValueStore.size())
        assertEquals(houseOne, keyValueStore.get(10))
    }

    @Test
    fun testRemoveItemRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStoreClient.delete(10)

        assertEquals(0, keyValueStore.size())
    }

    @Test
    fun testRemoveItemRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)
        keyValueStoreClient.delete(10)

        assertEquals(1, keyValueStore.size())
        assertEquals(houseTwo, keyValueStore.get(5))
    }

    @Test
    fun testGetReturnsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        assertEquals(houseTwo, keyValueStoreClient.get(5))
    }

    @Test
    fun getAllReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(KeyValue::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, KeyValue>(KeyValue::class.java)
        val keyValueStoreClient = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        val getAllResults = keyValueStoreClient.getAll()
        assertEquals(2, getAllResults.size)
        assertEquals(houseOne, getAllResults[10])
        assertEquals(houseTwo, getAllResults[5])
    }
}