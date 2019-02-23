package localDeployment

import org.junit.jupiter.api.Test
import testing.LocalNimbusDeployment
import localDeployment.exampleModels.House
import localDeployment.exampleModels.Person
import kotlin.test.assertEquals

class KeyValueStoreClientLocalTest {

    private val houseOne = House("testHouse", listOf(Person("TestPerson", 22)))
    private val houseTwo = House("testHouse2", listOf(Person("TestPerson2", 21)))

    @Test
    fun testAddIncreasesSizeAndAddsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(House::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, House>(House::class.java)
        val keyValueStoreClient = localDeployment.getKeyValueStoreClient(Int::class.java, House::class.java)

        assertEquals(0, keyValueStore.size())

        keyValueStoreClient.put(10, houseOne)

        assertEquals(1, keyValueStore.size())
        assertEquals(houseOne, keyValueStore.get(10))
    }

    @Test
    fun testRemoveItemRemovesItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(House::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, House>(House::class.java)
        val keyValueStoreClient = localDeployment.getKeyValueStoreClient(Int::class.java, House::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStoreClient.delete(10)

        assertEquals(0, keyValueStore.size())
    }

    @Test
    fun testRemoveItemRemovesCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(House::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, House>(House::class.java)
        val keyValueStoreClient = localDeployment.getKeyValueStoreClient(Int::class.java, House::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)
        keyValueStoreClient.delete(10)

        assertEquals(1, keyValueStore.size())
        assertEquals(houseTwo, keyValueStore.get(5))
    }

    @Test
    fun testGetReturnsCorrectItem() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(House::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, House>(House::class.java)
        val keyValueStoreClient = localDeployment.getKeyValueStoreClient(Int::class.java, House::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        assertEquals(houseTwo, keyValueStoreClient.get(5))
    }

    @Test
    fun getAllReturnsCorrectValues() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(House::class.java)

        val keyValueStore = localDeployment.getKeyValueStore<Int, House>(House::class.java)
        val keyValueStoreClient = localDeployment.getKeyValueStoreClient(Int::class.java, House::class.java)

        keyValueStore.put(10, houseOne)
        keyValueStore.put(5, houseTwo)

        val getAllResults = keyValueStoreClient.getAll()
        assertEquals(2, getAllResults.size)
        assertEquals(houseOne, getAllResults[10])
        assertEquals(houseTwo, getAllResults[5])
    }
}