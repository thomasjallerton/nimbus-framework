package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction
import com.nimbusframework.nimbuscore.clients.notification.NotificationTopicAnnotationService
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.NotificationFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.notification.NotificationMethod
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalNotificationFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {
        val notificationServerlessFunctions = method.getAnnotationsByType(NotificationServerlessFunction::class.java)
        if (notificationServerlessFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val annotation = stageService.annotationForStage(notificationServerlessFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val notificationMethod = NotificationMethod(method, invokeOn)
            val topicName = NotificationTopicAnnotationService.getTopicName(annotation.notificationTopic.java, stageService.deployingStage)
            val functionInformation = NotificationFunctionInformation(topicName)
            val notificationTopic = localResourceHolder.notificationTopics[topicName]!!

            notificationTopic.addSubscriber(notificationMethod)
            localResourceHolder.functions[functionIdentifier] = ServerlessFunction(notificationMethod, functionInformation)
        }
        return true
    }

}