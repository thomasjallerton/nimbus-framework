package com.nimbusframework.nimbusaws.cloudformation.resource

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonObject

class NimbusBucketResource(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {

    override fun getName(): String {
        return "NimbusDeploymentBucket"
    }

    override fun toCloudFormation(): JsonObject {
        val bucketResource = JsonObject()
        bucketResource.addProperty("Type","AWS::S3::Bucket")
        return bucketResource
    }
}