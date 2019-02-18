package clients.document

import com.fasterxml.jackson.databind.ObjectMapper
import testing.LocalNimbusDeployment

class DocumentStoreClientLocal<T>(private val clazz: Class<T>): DocumentStoreClient<T>(clazz) {

    private val localNimbusDeployment = LocalNimbusDeployment.getInstance()
    private val documentStore = localNimbusDeployment.getDocumentStore(clazz)
    private val objectMapper = ObjectMapper()

    override fun put(obj: T) {
        val key = getKeyValue(obj)
        val value = allAttributesToJson(obj)
        documentStore[key] = value
    }

    override fun delete(obj: T) {
        val key = getKeyValue(obj)
        documentStore.remove(key)
    }

    override fun deleteKey(keyObj: Any) {
        documentStore.remove(keyObj)
    }

    override fun getAll(): List<T> {
        return documentStore.values.map { value -> objectMapper.readValue(value, clazz) }
    }

    override fun get(keyObj: Any): T? {
        val strValue = documentStore[keyObj]
        return if (strValue != null) {
            objectMapper.readValue(strValue, clazz)
        } else {
            null
        }
    }

    private fun allAttributesToJson(obj: T): String {
        val attributeMap: MutableMap<String, Any?> = mutableMapOf()

        for ((fieldName, field) in allAttributes) {
            field.isAccessible = true
            attributeMap[fieldName] = field.get(obj)
        }

        return objectMapper.writeValueAsString(attributeMap)
    }

    private fun getKeyValue(obj: T): Any {
        if (keys.size > 1) {
            throw Exception("Composite key shouldn't exist!!")
        } else if (keys.isEmpty()) {
            throw Exception("Need a key field!")
        }

        for ((_, field) in keys) {
            field.isAccessible = true
            return field[obj]
        }
        //should never hit here
        return Any()
    }



}