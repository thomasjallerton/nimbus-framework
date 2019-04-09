package com.nimbusframework.nimbuscore.clients.keyvalue

import com.nimbusframework.nimbuscore.clients.LocalClient
import com.nimbusframework.nimbuscore.clients.PermissionException
import com.nimbusframework.nimbuscore.clients.file.FileStorageClient
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.keyvalue.LocalKeyValueStore

internal class KeyValueStoreClientLocal<K, V>(
        private val valueClass: Class<V>
): KeyValueStoreClient<K, V>, LocalClient() {

    private val table: LocalKeyValueStore<K, V> = localNimbusDeployment.getKeyValueStore(valueClass)

    override fun canUse(): Boolean {
        return checkPermissions(PermissionType.KEY_VALUE_STORE, valueClass.canonicalName)
    }
    override val clientName: String = KeyValueStoreClientLocal::class.java.simpleName

    override fun put(key: K, value: V) {
        checkClientUse()
        table.put(key, value)
    }

    override fun delete(keyObj: K) {
        checkClientUse()
        table.delete(keyObj)
    }

    override fun getAll(): Map<K, V> {
        checkClientUse()
        return table.getAll()
    }

    override fun get(keyObj: K): V? {
        checkClientUse()
        return table.get(keyObj)
    }
}