package com.nimbusframework.nimbuscore.testing.queue

import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.wrappers.queue.models.QueueEvent
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.*

class QueueMethod(private val method: Method, private val invokeOn: Any, internal val batchSize: Int) : ServerlessMethod(method, QueueEvent::class.java) {

    private val isListParams = method.parameterTypes.any { clazz -> isListType(clazz.canonicalName) }
    private val paramType: Class<*>? = if (isListParams) {
        val type = method.genericParameterTypes.find {
            if (it is ParameterizedType) {
                val clazz = it.actualTypeArguments[0] as Class<out Any>
                clazz != QueueEvent::class.java
            } else {
                false
            }
        }
        (type as ParameterizedType).actualTypeArguments[0] as Class<out Any>
    } else {
        method.parameterTypes.find { clazz -> clazz.canonicalName != QueueEvent::class.java.canonicalName }
    }


    internal fun invoke(obj: Any) {
        timesInvoked++

        if (obj is List<*>) {
            val parsedList = obj.map {
                if (paramType != null) {
                    objectMapper.readValue(objectMapper.writeValueAsString(it), paramType)
                } else {
                    obj
                }
            }
            if (isListParams) {
                invokeList(parsedList)
            } else {
                val event = QueueEvent()
                parsedList.forEach {
                    invokeGeneral(it, event)
                }
            }
        } else {

            val parsedObject = objectMapper.readValue(objectMapper.writeValueAsString(obj), paramType)

            if (isListParams) {
                invokeList(parsedObject)
            } else {
                invokeGeneral(parsedObject, QueueEvent())
            }
        }

    }

    private fun invokeList(obj: Any) {
        val list = if (obj is List<*>) {
            obj
        } else {
            listOf(obj)
        }
        val requestId = UUID.randomUUID().toString()
        val eventList: MutableList<QueueEvent> = mutableListOf()
        list.forEach { _ ->
            eventList.add(QueueEvent(requestId = requestId
            ))
        }

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