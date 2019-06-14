package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.file.UsesFileStorage
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.function.permissions.FileStoragePermission
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalUsesFileStorageClientHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesFileStorages = method.getAnnotationsByType(UsesFileStorage::class.java)

        for (usesFileStorage in usesFileStorages) {
            if (usesFileStorage.stages.contains(stage)) {
                functionEnvironment.addPermission(PermissionType.FILE_STORAGE, FileStoragePermission(usesFileStorage.bucketName))
            }
        }
    }

}