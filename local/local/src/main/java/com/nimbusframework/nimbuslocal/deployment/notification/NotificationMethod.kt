package com.nimbusframework.nimbuslocal.deployment.notification

import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.NotificationEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import com.nimbusframework.nimbuslocal.deployment.webserver.webconsole.models.FunctionSubscriberInformation
import java.lang.reflect.Method

class NotificationMethod(
        private val method: Method,
        private val invokeOn: Any
): ServerlessMethod(
        method,
        NotificationEvent::class.java,
        FunctionType.NOTIFICATION
) {

    fun getFunctionSubscriber(): FunctionSubscriberInformation {
        return FunctionSubscriberInformation(
                method.declaringClass.simpleName,
                method.name
        )
    }

    fun invoke(strParam: String) {

        val notificationEvent = NotificationEvent()
        timesInvoked++
        val inputIndex = inputIndex()
        val param = if (inputIndex != -1) {
            val clazz = method.parameterTypes[inputIndex]
            JacksonClient.readValue(strParam, clazz)
        } else {
            null
        }
        mostRecentInvokeArgument = param

        val params = method.parameters
        mostRecentValueReturned = if (params.isEmpty()) {
            method.invoke(invokeOn)
        } else if (params.size == 2) {
            if (eventIndex() == 0) {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, notificationEvent, param)
            } else {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param, notificationEvent)
            }
        } else if (params.size == 1) {
            if (eventIndex() == 0) {
                method.invoke(invokeOn, notificationEvent)
            } else {
                mostRecentInvokeArgument = param
                method.invoke(invokeOn, param)
            }
        } else {
            throw Exception("Wrong number of params, shouldn't have compiled")
        }
    }
}
