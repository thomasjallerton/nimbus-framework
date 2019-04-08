package com.nimbusframework.nimbuscore.cloudformation.outputs

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.NimbusBucketResource
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.configuration.DEPLOYMENT_BUCKET_NAME

class BucketNameOutput(
        private val bucketResource: NimbusBucketResource,
        nimbusState: NimbusState
): Output(nimbusState, bucketResource.stage) {

    override fun getExportName(): String {
        return "${nimbusState.projectName}-$stage-$DEPLOYMENT_BUCKET_NAME"
    }

    override fun getName(): String {
        return DEPLOYMENT_BUCKET_NAME
    }

    override fun toCloudFormation(): JsonObject {
        val bucketName = JsonObject()
        val value = JsonObject()

        value.addProperty("Ref", bucketResource.getName())
        bucketName.add("Value", value)

        val export = JsonObject()
        export.addProperty("Name", getExportName())

        bucketName.add("Export", export)

        return bucketName
    }
}
