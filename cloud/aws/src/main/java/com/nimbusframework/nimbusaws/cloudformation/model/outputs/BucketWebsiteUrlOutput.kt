package com.nimbusframework.nimbusaws.cloudformation.model.outputs

import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

class BucketWebsiteUrlOutput(
    private val bucket: FileBucketResource,
    nimbusState: NimbusState
) : Output(nimbusState, bucket.stage) {

    override fun getExportName(): String {
        return "${nimbusState.projectName}-$stage-${getName()}"
    }

    override fun toCloudFormation(): JsonObject {
        val restApiUrl = JsonObject()

        restApiUrl.add("Value", bucket.getAttr("WebsiteURL"))

        val export = JsonObject()
        export.addProperty("Name", getExportName())

        restApiUrl.add("Export", export)

        return restApiUrl
    }

    override fun getName(): String {
        return "${bucket.getName()}WebsiteUrl"
    }
}
