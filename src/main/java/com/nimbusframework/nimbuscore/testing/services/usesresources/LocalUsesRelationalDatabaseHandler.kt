package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.database.UsesRelationalDatabase
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.function.permissions.StorePermission
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalUsesRelationalDatabaseHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesRelationalDatabases = method.getAnnotationsByType(UsesRelationalDatabase::class.java)

        for (usesRelationalDatabase in usesRelationalDatabases) {
            if (usesRelationalDatabase.stages.contains(stage)) {
                functionEnvironment.addPermission(PermissionType.RELATIONAL_DATABASE, StorePermission(usesRelationalDatabase.dataModel.qualifiedName.toString()))
            }
        }
    }

}