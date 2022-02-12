package com.nimbusframework.nimbuslocal.deployment.services.function

import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.clients.file.FileStorageBucketNameAnnotationService
import com.nimbusframework.nimbuslocal.deployment.file.FileStorageMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.function.ServerlessFunction
import com.nimbusframework.nimbuslocal.deployment.function.information.FileStorageFunctionInformation
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import java.lang.reflect.Method

class LocalFileStorageFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stageService: StageService
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method): Boolean {

        val fileStorageFunctions = method.getAnnotationsByType(FileStorageServerlessFunction::class.java)
        if (fileStorageFunctions.isEmpty()) return false

        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val fileStorage = localResourceHolder.fileStorage
        val methods = localResourceHolder.functions

        val annotation = stageService.annotationForStage(fileStorageFunctions) {annotation -> annotation.stages}
        if (annotation != null) {
            val invokeOn = getFunctionClassInstance(clazz)

            val bucketName = FileStorageBucketNameAnnotationService.getBucketName(annotation.fileStorageBucket.java, stageService.deployingStage)
            val localFileStorage = fileStorage[bucketName]!!
            val fileStorageMethod = FileStorageMethod(method, invokeOn, annotation.eventType)
            val functionInformation = FileStorageFunctionInformation(
                    bucketName,
                    annotation.eventType
            )
            localFileStorage.addMethod(fileStorageMethod)
            methods[functionIdentifier] = ServerlessFunction(fileStorageMethod, functionInformation)
        }
        return true
    }

}