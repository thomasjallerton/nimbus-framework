package com.nimbusframework.nimbuslocal.deployment.queue

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.*

class QueueMethod(
        private val method: Method,
        private val invokeOn: Any,
        internal val batchSize: Int
) : ServerlessMethod(method, QueueEvent::class.java, FunctionType.QUEUE) {

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

    internal fun invokeJson(json: String, messageId: Int): Any {
        val obj = JacksonClient.readValue(json, paramType!!)
        invoke(obj, messageId)
        return obj
    }

    internal fun invoke(obj: Any, messageId: Int) {
        timesInvoked++

        if (obj is List<*>) {
            val parsedList = obj.map {
                if (paramType != null) {
                    JacksonClient.readValue(JacksonClient.writeValueAsString(it), paramType)
                } else {
                    obj
                }
            }
            if (isListParams) {
                invokeList(parsedList, messageId)
            } else {
                val requestId = UUID.randomUUID().toString()
                val event = QueueEvent(messageId = messageId.toString(), requestId = requestId)
                parsedList.forEach {
                    invokeGeneral(it, event)
                }
            }
        } else {

            val parsedObject = JacksonClient.readValue(JacksonClient.writeValueAsString(obj), paramType!!)
            val requestId = UUID.randomUUID().toString()
            val event = QueueEvent(messageId = messageId.toString(), requestId = requestId)

            if (isListParams) {
                invokeList(parsedObject, messageId)
            } else {
                invokeGeneral(parsedObject, event)
            }
        }

    }

    private fun invokeList(obj: Any, messageId: Int) {
        val list = if (obj is List<*>) {
            obj
        } else {
            listOf(obj)
        }
        val requestId = UUID.randomUUID().toString()
        val eventList: MutableList<QueueEvent> = mutableListOf()
        list.forEachIndexed { index, _ ->
            eventList.add(
                    QueueEvent(requestId = requestId, messageId = (messageId + index).toString())
            )
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
