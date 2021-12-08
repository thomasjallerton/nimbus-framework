package com.nimbusframework.nimbusawslocal.deployment.services.resource

import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import exampleresources.DynamoDbDocument
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldNotBe

internal class LocalDynamoDbDocumentStoreCreatorTest: AnnotationSpec() {

    @Test
    fun canCorrectlyDetectAnnotation() {
        val localNimbusDeployment = LocalNimbusDeployment.getNewInstance("exampleresources")
        val document = localNimbusDeployment.getDocumentStore(DynamoDbDocument::class.java);
        document shouldNotBe null
    }
}