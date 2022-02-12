package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest
import com.nimbusframework.nimbuscore.clients.store.WriteItemRequest
import com.nimbusframework.nimbuscore.clients.store.conditions.Condition
import com.nimbusframework.nimbuscore.exceptions.RetryableException
import com.nimbusframework.nimbuscore.exceptions.StoreConditionException
import java.lang.reflect.Field
import java.util.*

class LocalStore<K, V>(
        keyClass: Class<K>,
        private val valueClass: Class<V>,
        private val primaryKeyColumn: String,
        private val allAttributes: Map<String, Field>): LocalStoreTransactions {

    private var localStore: MutableMap<K, String> = mutableMapOf()

    private var originalTransactionStore: MutableMap<K, String> = mutableMapOf()
    private var currentTransaction: UUID? = null

    private val methods: MutableList<StoreMethod> = mutableListOf()

    private val conditionProcessor = ConditionProcessor(valueClass, primaryKeyColumn)

    internal fun addMethod(method: StoreMethod) {
        methods.add(method)
    }

    fun size(): Int {
        return localStore.size
    }

    fun delete(key: K, condition: Condition) {
        checkCondition(key, condition)
        delete(key)
    }

    fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return WriteItemRequestLocal(this) {
            updateValue(key, numericFieldName, amount, false)
        }
    }

    fun getDecrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return WriteItemRequestLocal(this) {
            checkCondition(key, condition)
            updateValue(key, numericFieldName, amount, false)
        }
    }

    fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number): WriteItemRequest {
        return WriteItemRequestLocal(this) {
            updateValue(key, numericFieldName, amount, true)
        }
    }

    fun getIncrementValueRequest(key: K, numericFieldName: String, amount: Number, condition: Condition): WriteItemRequest {
        return WriteItemRequestLocal(this) {
            checkCondition(key, condition)
            updateValue(key, numericFieldName, amount, true)
        }
    }

    fun getDeleteKeyItemRequest(key: K): WriteItemRequest {
        return WriteItemRequestLocal(this) { delete(key) }
    }

    fun getDeleteKeyItemRequest(key: K, condition: Condition): WriteItemRequest {
        return WriteItemRequestLocal(this) { delete(key, condition) }
    }

    fun getReadItem(key: K): ReadItemRequest<V> {
        return ReadItemRequestLocal { get(key) }
    }

    fun getWriteItem(key: K, value: V): WriteItemRequest {
        return WriteItemRequestLocal(this) { put(key, value) }
    }

    fun getWriteItem(key: K, value: V, condition: Condition): WriteItemRequest {
        return WriteItemRequestLocal(this) { put(key, value, condition) }
    }

    fun put(key: K, value: V, condition: Condition) {
        checkCondition(key, condition)
        put(key, value)
    }


    fun put(key: K, valueObj: V) {
        val value = allAttributesToJson(valueObj)

        if (localStore.containsKey(key)) {
            val oldItem = JacksonClient.readValue(localStore[key], valueClass)
            localStore[key] = value
            methods.forEach { method -> method.invokeModify(oldItem, valueObj) }

        } else {
            localStore[key] = value
            methods.forEach { method -> method.invokeInsert(valueObj) }
        }
    }

    fun delete(key: K) {
        if (localStore.containsKey(key)) {
            val removedItemStr = localStore.remove(key)
            val oldItem = JacksonClient.readValue(removedItemStr, valueClass)
            methods.forEach { method -> method.invokeRemove(oldItem) }
        }
    }


    fun getAll(): Map<K, V> {
        return localStore.mapValues { entry -> JacksonClient.readValue(entry.value, valueClass)}
    }

    fun get(key: K): V? {
        val strValue = localStore[key]
        return if (strValue != null) {
            JacksonClient.readValue(strValue, valueClass)
        } else {
            null
        }
    }

    private fun allAttributesToJson(obj: V): String {
        val attributeMap: MutableMap<String, Any?> = mutableMapOf()

        for ((fieldName, field) in allAttributes) {
            field.isAccessible = true
            attributeMap[fieldName] = field.get(obj)
        }

        return JacksonClient.writeValueAsString(attributeMap)
    }


    private fun checkCondition(key: K, condition: Condition) {
        val existingItemStr = localStore[key]
        val existingItem = try {
            JacksonClient.readValue(existingItemStr, valueClass)
        } catch (e: Exception) {
            null
        }
        if (!conditionProcessor.processCondition(condition, existingItem)) {
            throw StoreConditionException()
        }
    }

    private fun updateValue(key: K, numericFieldName: String, amount: Number, isIncrement: Boolean) {
        val obj = get(key)
        if (obj != null) {
            val field = valueClass.getDeclaredField(numericFieldName)
            field.isAccessible = true
            val fieldVal = field[obj]
            if (fieldVal is Number) {
                when (fieldVal) {
                    is Double -> field.set(obj, if (isIncrement) fieldVal.toDouble() + amount.toDouble() else fieldVal.toDouble() - amount.toDouble() )
                    is Float -> field.set(obj, if (isIncrement) fieldVal.toFloat() + amount.toFloat() else fieldVal.toFloat() - amount.toFloat() )
                    is Long -> field.set(obj, if (isIncrement) fieldVal.toLong() + amount.toLong() else fieldVal.toLong() - amount.toLong() )
                    is Int -> field.set(obj, if (isIncrement) fieldVal.toInt() + amount.toInt() else fieldVal.toInt() - amount.toInt() )
                    is Short -> field.set(obj, if (isIncrement) (fieldVal.toShort() + amount.toShort()).toShort() else (fieldVal.toShort() - amount.toShort()).toShort() )
                    is Byte -> field.set(obj, if (isIncrement) fieldVal.toByte() + amount.toByte() else fieldVal.toByte() - amount.toByte() )
                }
            }
            put(key, obj)
        }
    }

    override fun startTransaction(transactionUid: UUID) {
        if (currentTransaction != transactionUid) {
            if (currentTransaction != null) throw RetryableException("There is an ongoing transaction")
            originalTransactionStore = localStore.toMutableMap()
            currentTransaction = transactionUid
        }
    }

    override fun successfulTransaction(transactionUid: UUID) {
        if (currentTransaction == transactionUid) {
            currentTransaction = null
        }
    }

    override fun failedTransaction(transactionUid: UUID) {
        if (currentTransaction == transactionUid) {
            localStore = originalTransactionStore.toMutableMap()
            currentTransaction = null
        }
    }
}
