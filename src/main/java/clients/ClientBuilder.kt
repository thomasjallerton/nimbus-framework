package clients

import clients.document.DocumentStoreClient
import clients.document.DocumentStoreClientDynamo
import clients.keyvalue.KeyValueStoreClient
import clients.keyvalue.KeyValueStoreClientDynamo
import clients.keyvalue.KeyValueStoreClientLocal
import clients.queue.QueueClient
import clients.queue.QueueClientDynamo
import clients.queue.QueueClientLocal
import testing.LocalNimbusDeployment

object ClientBuilder {

    @JvmStatic
    fun <K, V> getKeyValueStoreClient(key: Class<K>, value: Class<V>): KeyValueStoreClient<K, V> {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            KeyValueStoreClientLocal(key, value)
        } else {
            KeyValueStoreClientDynamo(key, value)
        }
    }

    @JvmStatic
    fun <T> getDocumentStoreClient(document: Class<T>): DocumentStoreClient<T> {
        return DocumentStoreClientDynamo(document)
    }

    @JvmStatic
    fun getQueueClient(id: String): QueueClient {
        return if (LocalNimbusDeployment.isLocalDeployment) {
            QueueClientLocal(id)
        } else {
            QueueClientDynamo(id)
        }
    }
}
