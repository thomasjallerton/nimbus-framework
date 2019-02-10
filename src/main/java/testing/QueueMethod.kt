package testing

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.javaws.exceptions.InvalidArgumentException
import wrappers.queue.models.QueueEvent
import java.lang.Exception
import java.lang.reflect.Method

class QueueMethod(private val method: Method, private val invokeOn: Any): GeneralMethod() {

    private val objectMapper = ObjectMapper()

    fun invoke(obj: Any) {
        timesInvoked++

        val params = method.parameters
        val queueEvent = QueueEvent()

        val str = objectMapper.writeValueAsString(obj)

        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (params[0].type.simpleName.contains(QueueEvent::class.java.canonicalName)) {
                val parsedObj = objectMapper.readValue(str, params[1].type)
                mostRecentInvokeArgument = parsedObj
                method.invoke(invokeOn, queueEvent, parsedObj)
            } else {
                val parsedObj = objectMapper.readValue(str, params[0].type)
                mostRecentInvokeArgument = parsedObj
                method.invoke(invokeOn, parsedObj, queueEvent)
            }
        } else if (params.size == 1) {
            if (params[0].type.simpleName.contains(QueueEvent::class.java.canonicalName)) {
                method.invoke(invokeOn, queueEvent)
            } else {
                val parsedObj = objectMapper.readValue(str, params[0].type)
                mostRecentInvokeArgument = parsedObj
                method.invoke(invokeOn, parsedObj)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
    }
}