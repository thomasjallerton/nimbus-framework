package com.nimbusframework.nimbusawslocal.deployment.services.resource

import com.nimbusframework.nimbusawslocal.aws.AwsSpecificLocalDeployment
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import exampleresources.DynamoDbKeyValue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldNotBe

internal class LocalDynamoDbKeyValueStoreCreatorTest: AnnotationSpec() {

    @Test
    fun canCorrectlyDetectAnnotation() {
        val localNimbusDeployment = LocalNimbusDeployment.getNewInstance("exampleresources", AwsSpecificLocalDeployment.newInstance())
        val keyValueStore = localNimbusDeployment.getKeyValueStore<Int, DynamoDbKeyValue>(DynamoDbKeyValue::class.java);
        keyValueStore shouldNotBe null
    }
}
