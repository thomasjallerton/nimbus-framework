package testing

import java.util.*

class LocalQueue(private val target: QueueMethod) {

    private val queue: Queue<Any> = LinkedList()
    private var itemsAdded: Int = 0

    fun add(obj: Any){
        queue.add(obj)
        target.invoke(obj)
        itemsAdded++
    }

    fun getNumberOfItemsAdded(): Int {
        return itemsAdded
    }

    fun getNumberOfItems(): Int {
        return queue.size
    }
}