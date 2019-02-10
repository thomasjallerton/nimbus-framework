package clients.document

interface DocumentStoreClient<T> {
    fun put(obj: T)

    fun delete(obj: T)

    fun deleteKey(keyObj: Any)

    fun getAll(): List<T>

    fun get(keyObj: Any): T?
}