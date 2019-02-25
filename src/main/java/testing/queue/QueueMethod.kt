package testing.queue

import testing.ServerlessMethod
import wrappers.queue.models.QueueEvent
import java.lang.reflect.Method

class QueueMethod(private val method: Method, private val invokeOn: Any, internal val batchSize: Int) : ServerlessMethod(method, QueueEvent::class.java) {

    internal val isListParams = method.parameterTypes.any { clazz -> isListType(clazz.canonicalName) }
    private val paramType: Class<*>?

    init {
        val nonEventClass = method.parameterTypes.find { clazz -> clazz.canonicalName != QueueEvent::class.java.canonicalName }
        paramType = if (isListParams) {
            null
        } else {
            nonEventClass
        }
    }

    fun invoke(obj: Any) {
        timesInvoked++

        if (isListParams) {
            invokeList(obj)
        } else {
            val parsedObject = if (paramType != null) {
                objectMapper.readValue(objectMapper.writeValueAsString(obj), paramType)
            } else {
                obj
            }
            invokeGeneral(parsedObject, QueueEvent())
        }
    }

    private fun invokeList(obj: Any) {
        val list = if (obj is List<*>) {
            obj
        } else {
            listOf(obj)
        }

        val eventList: MutableList<QueueEvent> = mutableListOf()
        list.forEach { _ -> eventList.add(QueueEvent()) }

        invokeGeneral(list, eventList)
    }

    private fun invokeGeneral(param: Any, queueEvent: Any) {
        val params = method.parameters
        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (eventIndex() == 0) {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, queueEvent, param)
            } else {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, queueEvent)
            }
        } else if (params.size == 1) {
            if (eventIndex() == 0) {
                method.invoke(invokeOn, queueEvent)
            } else {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
    }

    private fun isListType(typeName: String): Boolean {
        return typeName.startsWith("java.util.List") || typeName.startsWith("java.util.ArrayList") || typeName.startsWith("java.util.LinkedList")
    }

}