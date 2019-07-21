package com.nimbusframework.nimbuscore.testing.queue

import kotlin.random.Random

class LocalQueue {

    private val consumers: MutableList<QueueMethod> = mutableListOf()
    private var itemsAdded: MutableSet<in Any> = mutableSetOf()


    fun add(obj: Any) {
        getRandomConsumer().invoke(obj, itemsAdded.size)
        itemsAdded.add(obj)
    }

    fun addJson(json: String) {
        val addedObj = getRandomConsumer().invokeJson(json, itemsAdded.size)
        itemsAdded.add(addedObj)
    }

    fun addBatch(toAdd: List<Any>) {
        itemsAdded.addAll(toAdd)
        consumeBatch(toAdd)
    }

    private fun consumeBatch(queue: List<Any>) {
        if (queue.isEmpty()) return

        val target = getRandomConsumer()
        if (target.batchSize == 1) {
            target.invoke(queue[0], itemsAdded.size)
            if (queue.size == 1) return
            consumeBatch(queue.subList(1, queue.size))
        } else {
            val invokeList: MutableList<Any> = mutableListOf()
            val remaining: MutableList<Any> = mutableListOf()
            for ((index, queueItem) in queue.withIndex()) {
                if (index < target.batchSize) {
                    invokeList.add(queueItem)
                } else {
                    remaining.add(queueItem)
                }
            }
            target.invoke(invokeList, itemsAdded.size)
            consumeBatch(remaining)
        }
    }

    fun getNumberOfItemsAdded(): Int {
        return itemsAdded.size
    }

    fun itemsAddedContains(item: Any): Boolean {
        return itemsAdded.contains(item)
    }

    fun addConsumer(queueMethod: QueueMethod) {
        consumers.add(queueMethod)
    }

    private fun getRandomConsumer(): QueueMethod {
        return consumers[Random.nextInt(0, consumers.size)]
    }
}