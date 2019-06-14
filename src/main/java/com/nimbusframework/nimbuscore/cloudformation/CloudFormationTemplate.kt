package com.nimbusframework.nimbuscore.cloudformation

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.cloudformation.outputs.BucketNameOutput
import com.nimbusframework.nimbuscore.cloudformation.outputs.OutputCollection
import com.nimbusframework.nimbuscore.cloudformation.resource.NimbusBucketResource
import com.nimbusframework.nimbuscore.cloudformation.resource.ResourceCollection
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbuscore.cloudformation.resource.http.ApiGatewayDeployment
import com.nimbusframework.nimbuscore.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.WebSocketApi
import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.WebSocketDeployment
import com.nimbusframework.nimbuscore.persisted.NimbusState

data class CloudFormationTemplate(
        private val nimbusState: NimbusState,
        private val stage: String,

        val resources: ResourceCollection = ResourceCollection(),
        val outputs: OutputCollection = OutputCollection(),

        val fileBucketWebsites: MutableList<FileBucket> = mutableListOf(),
        var rootRestApi: RestApi? = null,
        var apiGatewayDeployment: ApiGatewayDeployment? = null,
        var rootWebSocketApi: WebSocketApi? = null,
        var webSocketDeployment: WebSocketDeployment? = null

) {


    init {
        val bucket = NimbusBucketResource(nimbusState, stage)
        val bucketNameOutput = BucketNameOutput(bucket, nimbusState)
        resources.addResource(bucket)
        outputs.addOutput(bucketNameOutput)
    }


    fun referencedFileStorageBucket(origin: String): FileBucket? {
        if (origin == "") return null
        for (website in fileBucketWebsites) {
            val websiteSubstitution = "\${${website.annotationBucketName.toUpperCase()}_URL}"
            if (origin == websiteSubstitution) {
                return website
            }
        }
        return null
    }

    fun valid(): Boolean {
        return !resources.isEmpty()
    }

    fun toJson(): String {
        val template = JsonObject()

        template.addProperty("AWSTemplateFormatVersion", "2010-09-09")
        template.addProperty("Description", "The AWS CloudFormation template for this Nimbus application")
        template.add("Resources", resources.toJson())

        if (!outputs.isEmpty()) {
            template.add("Outputs", outputs.toJson())
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(template)
    }

}