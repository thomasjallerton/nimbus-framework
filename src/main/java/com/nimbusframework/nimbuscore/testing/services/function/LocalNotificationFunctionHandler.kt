package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.notification.LocalNotificationTopic
import com.nimbusframework.nimbuscore.testing.notification.NotificationMethod
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalNotificationFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val notificationServerlessFunctions = method.getAnnotationsByType(NotificationServerlessFunction::class.java)
        if (notificationServerlessFunctions.isEmpty()) return false

        for (notificationFunction in notificationServerlessFunctions) {
            if (notificationFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                val notificationMethod = NotificationMethod(method, invokeOn)

                val notificationTopic = localResourceHolder.notificationTopics.getOrPut(notificationFunction.topic) { LocalNotificationTopic() }

                notificationTopic.addSubscriber(notificationMethod)
                localResourceHolder.methods[functionIdentifier] = notificationMethod
            }
        }
        return true
    }

}