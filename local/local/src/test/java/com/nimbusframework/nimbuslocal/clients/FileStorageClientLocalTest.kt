package com.nimbusframework.nimbuslocal.clients

import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import com.nimbusframework.nimbuslocal.exampleHandlers.ExampleFileStorageHandler
import com.nimbusframework.nimbuslocal.exampleModels.Bucket
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.io.File

class FileStorageClientLocalTest: AnnotationSpec() {

    @Test
    fun testTriggeredOnNewFile() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "newFile")

        method.timesInvoked shouldBe 0

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "newFile"
        fileStorage.saveFile(path, "testContent")

        method.timesInvoked shouldBe 1

        val event = method.mostRecentInvokeArgument as FileStorageEvent
        event.key shouldBe path
        event.size shouldBe 11
    }

    @Test
    fun testTriggeredOnFileDeleted() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "deletedFile")

        method.timesInvoked shouldBe 0

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "newFile"
        fileStorage.saveFile(path, "testContent")
        fileStorage.deleteFile(path)

        method.timesInvoked shouldBe 1

        val event = method.mostRecentInvokeArgument as FileStorageEvent
        event.key shouldBe path
        event.size shouldBe 0
    }

    @Test
    fun notTriggeredIfFileDoesntExist() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(Bucket::class.java, ExampleFileStorageHandler::class.java)

        val method = localDeployment.getMethod(ExampleFileStorageHandler::class.java, "deletedFile")

        method.timesInvoked shouldBe 0

        val fileStorage = localDeployment.getLocalFileStorage(Bucket::class.java)
        val path = "testdir" + File.separator + "shouldnotexist"

        fileStorage.deleteFile(path)

        method.timesInvoked shouldBe 0
    }
}