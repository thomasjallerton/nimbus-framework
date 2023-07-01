package com.nimbusframework.nimbuslocal.deployment.services.usesresources

import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionEnvironment
import com.nimbusframework.nimbuslocal.deployment.function.permissions.FileStoragePermission
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalUsesFileStorageClientHandler(
        localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
): LocalUsesResourcesHandler(localResourceHolder) {

    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesFileStorages = method.getAnnotationsByType(UsesFileStorageBucket::class.java)

        val annotations = stageService.annotationsForStage(usesFileStorages) { annotation -> annotation.stages}
        for (annotation in annotations) {
            val bucketName = FileStorageBucketNameAnnotationService.getBucketName(annotation.fileStorageBucket.java, stageService.deployingStage)
            functionEnvironment.addPermission(PermissionType.FILE_STORAGE, FileStoragePermission(bucketName))
        }
    }

}
