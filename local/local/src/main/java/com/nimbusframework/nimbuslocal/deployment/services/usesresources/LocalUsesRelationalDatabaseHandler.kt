package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.database.UsesRelationalDatabase
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.StorePermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesRelationalDatabaseHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesRelationalDatabases = method.getAnnotationsByType(UsesRelationalDatabase::class.java)

        val annotation = stageService.annotationForStage(usesRelationalDatabases) { annotation -> annotation.stages}
        if (annotation != null) {
            functionEnvironment.addPermission(PermissionType.RELATIONAL_DATABASE, StorePermission(annotation.dataModel.qualifiedName.toString()))
        }
    }

}