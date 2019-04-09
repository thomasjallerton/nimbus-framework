package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.queue.UsesQueue
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.function.permissions.QueuePermission
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalUsesQueueHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesQueues = method.getAnnotationsByType(UsesQueue::class.java)

        for (usesQueue in usesQueues) {
            if (usesQueue.stages.contains(stage)) {
                functionEnvironment.addPermission(PermissionType.QUEUE, QueuePermission(usesQueue.id))
            }
        }
    }

}