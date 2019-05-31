package com.nimbusframework.nimbuscore.testing.document

import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuscore.testing.ServerlessMethod
import com.nimbusframework.nimbuscore.wrappers.store.models.StoreEvent
import java.lang.reflect.Method

class KeyValueMethod(private val method: Method, private val invokeOn: Any, private val type: StoreEventType) : ServerlessMethod(method, StoreEvent::class.java) {


    fun invokeInsert(newItem: Any?) {
        timesInvoked++

        if (type != StoreEventType.INSERT) return

        val event = StoreEvent(type.name)

        val params = method.parameters
        val eventIndex = eventIndex()

        mostRecentInvokeArgument = newItem
        mostRecentValueReturned = when {
            params.isEmpty() -> method.invoke(invokeOn)
            params.size == 1 && eventIndex == 0 -> method.invoke(invokeOn, event)
            params.size == 1 -> method.invoke(invokeOn, newItem)
            else -> { //Params.size == 2
                if (eventIndex == 0) {
                    method.invoke(invokeOn, event, newItem)
                } else {
                    method.invoke(invokeOn, newItem, event)
                }
            }
        }
    }

    fun invokeModify(oldItem: Any?, newItem: Any?) {
        timesInvoked++

        if (type != StoreEventType.MODIFY) return

        val event = StoreEvent(type.name)

        val params = method.parameters
        val eventIndex = eventIndex()

        mostRecentInvokeArgument = listOf(oldItem, newItem)
        mostRecentValueReturned = when {
            params.isEmpty() -> method.invoke(invokeOn)
            params.size == 1 -> when (eventIndex) {
                0 -> method.invoke(invokeOn, event)
                else -> method.invoke(invokeOn, newItem)
            }
            params.size == 2 -> {
                    when (eventIndex) {
                        1 -> method.invoke(invokeOn, newItem, event)
                        0 -> method.invoke(invokeOn, event, newItem)
                        else -> method.invoke(invokeOn, oldItem, newItem)
                    }
                }
            //Params.size == 3
            else -> method.invoke(invokeOn, oldItem, newItem, event)
        }
    }

    fun invokeRemove(oldItem: Any?) {
        timesInvoked++

        if (type != StoreEventType.REMOVE) return

        val event = StoreEvent(type.name)

        val params = method.parameters
        val eventIndex = eventIndex()

        mostRecentInvokeArgument = oldItem
        mostRecentValueReturned = when {
            params.isEmpty() -> method.invoke(invokeOn)
            params.size == 1 && eventIndex == 0 -> method.invoke(invokeOn, event)
            params.size == 1 -> method.invoke(invokeOn, oldItem)
            else -> { //Params.size == 2
                if (eventIndex == 0) {
                    method.invoke(invokeOn, event, oldItem)
                } else {
                    method.invoke(invokeOn, oldItem, event)
                }
            }
        }
    }

}