package com.nimbusframework.nimbuslocal.deployment.file

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType
import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import java.lang.reflect.Method

class FileStorageMethod(
        private val method: Method,
        private val invokeOn: Any,
        private val requiredEventType: FileStorageEventType
): ServerlessMethod(
        method,
        FileStorageEvent::class.java,
        FunctionType.FILE_STORAGE
) {


    fun invoke(filePath: String, fileSize: Long, fileStorageEventType: FileStorageEventType) {

        if (fileStorageEventType != requiredEventType) return

        val fileStorageEvent = FileStorageEvent(filePath, fileSize)
        timesInvoked++

        mostRecentInvokeArgument = fileStorageEvent

        val params = method.parameters
        mostRecentValueReturned = when {
            params.isEmpty() -> method.invoke(invokeOn)
            params.size == 1 -> method.invoke(invokeOn, fileStorageEvent)
            else -> throw Exception("Wrong number of params, shouldn't have compiled")
        }
    }
}