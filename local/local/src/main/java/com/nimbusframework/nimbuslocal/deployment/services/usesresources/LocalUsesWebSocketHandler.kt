package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.websocket.UsesServerlessFunctionWebSocket
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.AlwaysTruePermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesWebSocketHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesWebSocketManagers = method.getAnnotationsByType(UsesServerlessFunctionWebSocket::class.java)

        val annotation = stageService.annotationForStage(usesWebSocketManagers) { annotation -> annotation.stages}
        if (annotation != null) {
            functionEnvironment.addPermission(PermissionType.WEBSOCKET_MANAGER, AlwaysTruePermission())
        }
    }

}