package com.nimbusframework.nimbuscore.testing.document

import com.nimbusframework.nimbuscore.clients.document.AbstractDocumentStoreClient
import com.fasterxml.jackson.databind.ObjectMapper

class LocalDocumentStore<T>(private val clazz: Class<T>, stage: String): AbstractDocumentStoreClient<T>(clazz, stage) {

    private val documentStore: MutableMap<Any?, String> = mutableMapOf()
    private val objectMapper = ObjectMapper()
    private val methods: MutableList<DocumentMethod> = mutableListOf()

    internal fun addMethod(method: DocumentMethod) {
        methods.add(method)
    }

    fun size(): Int {return documentStore.size}

    override fun put(obj: T) {
        val key = getKeyValue(obj)
        val value = allAttributesToJson(obj)

        if (documentStore.containsKey(key)) {
            val oldItem = objectMapper.readValue(documentStore[key], clazz)
            documentStore[key] = value
            methods.forEach {method -> method.invokeModify(oldItem, obj)}

        } else {
            documentStore[key] = value
            methods.forEach {method -> method.invokeInsert(obj)}
        }
    }

    override fun delete(obj: T) {
        val key = getKeyValue(obj)

        if (documentStore.containsKey(key)) {
            val removedItemStr = documentStore.remove(key)
            val oldItem = objectMapper.readValue(removedItemStr, clazz)
            methods.forEach {method -> method.invokeRemove(oldItem)}
        }
    }

    override fun deleteKey(keyObj: Any) {
        if (documentStore.containsKey(keyObj)) {
            val removedItemStr = documentStore.remove(keyObj)
            val oldItem = objectMapper.readValue(removedItemStr, clazz)
            methods.forEach {method -> method.invokeRemove(oldItem)}
        }
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