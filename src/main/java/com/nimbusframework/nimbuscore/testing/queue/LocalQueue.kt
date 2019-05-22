package com.nimbusframework.nimbuscore.testing.queue

import kotlin.random.Random

class LocalQueue {

    private val consumers: MutableList<QueueMethod> = mutableListOf()
    private var itemsAdded: Int = 0

    fun add(obj: Any) {
        getRandomConsumer().invoke(obj)
        itemsAdded++
    }

    fun addBatch(toAdd: List<Any>) {
        itemsAdded += toAdd.size
        consumeBatch(toAdd)
    }

    private fun consumeBatch(queue: List<Any>) {
        if (queue.isEmpty()) return

        val target = getRandomConsumer()
        if (target.isListParams) {
            val invokeList: MutableList<Any> = mutableListOf()
            val remaining: MutableList<Any> = mutableListOf()
            for ((index, queueItem) in queue.withIndex()) {
                if (index < target.batchSize) {
                    invokeList.add(queueItem)
                } else {
                    remaining.add(queueItem)
                }
            }
            target.invoke(invokeList)
            consumeBatch(remaining)
        } else {
            target.invoke(queue[0])
            if (queue.size == 1) return
            consumeBatch(queue.subList(1, queue.size))
        }
    }

    fun getNumberOfItemsAdded(): Int {
        return itemsAdded
    }

    fun addConsumer(queueMethod: QueueMethod) {
        consumers.add(queueMethod)
    }

    private fun getRandomConsumer(): QueueMethod {
        return consumers[Random.nextInt(0, consumers.size)]
    }
}