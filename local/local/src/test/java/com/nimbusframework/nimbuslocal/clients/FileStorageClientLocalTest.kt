package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleFileStorageHandler
import com.nimbusframework.nimbuslocal.exampleModels.Bucket
import io.kotest.core.spec.style.AnnotationSpec
import java.io.File
import kotlin.test.assertEquals

class FileStorageClientLocalTest: AnnotationSpec() {

    @Test
    fun testTriggeredOnNewFile() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "newFile")

        assertEquals(0, method.timesInvoked)

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "newFile"
        fileStorage.saveFile(path, "testContent")

        assertEquals(1, method.timesInvoked)

        val event = method.mostRecentInvokeArgument as FileStorageEvent
        assertEquals(path, event.key)
        assertEquals(11, event.size)
    }

    @Test
    fun testTriggeredOnFileDeleted() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "deletedFile")

        assertEquals(0, method.timesInvoked)

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "newFile"
        fileStorage.saveFile(path, "testContent")
        fileStorage.deleteFile(path
        )
        assertEquals(1, method.timesInvoked)

        val event = method.mostRecentInvokeArgument as FileStorageEvent
        assertEquals(path, event.key)
        assertEquals(0, event.size)
    }

    @Test
    fun notTriggeredIfFileDoesntExist() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "deletedFile")

        assertEquals(0, method.timesInvoked)

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "shouldnotexist"

        fileStorage.deleteFile(path)

        assertEquals(0, method.timesInvoked)
    }
}