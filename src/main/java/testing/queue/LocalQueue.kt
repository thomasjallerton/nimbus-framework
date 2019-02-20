package testing.queue

import java.lang.Integer.min

class LocalQueue(private val target: QueueMethod) {

    private var itemsAdded: Int = 0

    fun add(obj: Any){
        target.invoke(obj)
        itemsAdded++
    }

    fun addBatch(toAdd: List<Any> ) {
        itemsAdded += toAdd.size
        if (target.isListParams) {
            for (i in 1 until toAdd.size step target.batchSize) {
                val invokeList: MutableList<Any> = mutableListOf()
                for (j in i..min(toAdd.size - 1, i + target.batchSize - 1)) {
                    invokeList.add(toAdd[j])
                }
                target.invoke(invokeList)
            }
        } else {
            toAdd.forEach {item -> target.invoke(item)}
        }
    }

    fun getNumberOfItemsAdded(): Int {
        return itemsAdded
    }
}