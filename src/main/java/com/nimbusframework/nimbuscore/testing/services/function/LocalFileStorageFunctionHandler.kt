package com.nimbusframework.nimbuscore.testing.services.function

import com.nimbusframework.nimbuscore.annotation.annotations.function.FileStorageServerlessFunction
import com.nimbusframework.nimbuscore.testing.file.FileStorageMethod
import com.nimbusframework.nimbuscore.testing.file.LocalFileStorage
import com.nimbusframework.nimbuscore.testing.function.FunctionIdentifier
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalFileStorageFunctionHandler(
        private val localResourceHolder: LocalResourceHolder,
        private val stage: String
) : LocalFunctionHandler(localResourceHolder) {

    override fun handleMethod(clazz: Class<out Any>, method: Method) {
        val functionIdentifier = FunctionIdentifier(clazz.canonicalName, method.name)

        val fileStorageFunctions = method.getAnnotationsByType(FileStorageServerlessFunction::class.java)

        val fileStorage = localResourceHolder.fileStorage
        val methods = localResourceHolder.methods

        for (fileStorageFunction in fileStorageFunctions) {
            if (fileStorageFunction.stages.contains(stage)) {
                val invokeOn = clazz.getConstructor().newInstance()

                if (!fileStorage.containsKey(fileStorageFunction.bucketName)) {
                    fileStorage[fileStorageFunction.bucketName] = LocalFileStorage(fileStorageFunction.bucketName)
                }
                val localFileStorage = fileStorage[fileStorageFunction.bucketName]
                val fileStorageMethod = FileStorageMethod(method, invokeOn, fileStorageFunction.eventType)
                localFileStorage!!.addMethod(fileStorageMethod)
                methods[functionIdentifier] = fileStorageMethod
            }
        }
    }

}