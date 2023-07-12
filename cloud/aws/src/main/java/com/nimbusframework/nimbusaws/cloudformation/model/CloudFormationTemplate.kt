package com.nimbusframework.nimbusaws.cloudformation.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.BucketNameOutput
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.OutputCollection
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.HttpApiOutput
import com.nimbusframework.nimbusaws.cloudformation.model.resource.NimbusBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ResourceCollection
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.ApiGatewayHttpDeployment
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.ApiGatewayHttpStage
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.HttpApi
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.HttpApiAuthorizer
import com.nimbusframework.nimbusaws.cloudformation.model.resource.websocket.WebSocketApi
import com.nimbusframework.nimbusaws.cloudformation.model.resource.websocket.WebSocketDeployment
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import java.util.*

data class CloudFormationTemplate(
    private val nimbusState: NimbusState,
    private val stage: String,

    val resources: ResourceCollection = ResourceCollection(),
    val outputs: OutputCollection = OutputCollection(),

    val fileBucketWebsites: MutableList<FileBucketResource> = mutableListOf(),
    private var rootHttpApi: HttpApi? = null,
    private var httpApiAuthorizer: HttpApiAuthorizer? = null,
//    private var apiGatewayHttpDeployment: ApiGatewayHttpDeployment? = null,
    var rootWebSocketApi: WebSocketApi? = null,
    var webSocketDeployment: WebSocketDeployment? = null
) {

    init {
        val bucket = NimbusBucketResource(nimbusState, stage)
        val bucketNameOutput = BucketNameOutput(bucket, nimbusState)
        resources.addResource(bucket)
        outputs.addOutput(bucketNameOutput)
    }

    fun addRestApiAuthorizer(authorizer: HttpApiAuthorizer) {
        httpApiAuthorizer = authorizer
        resources.addResource(authorizer)
    }

    fun getRestApiAuthorizer(): HttpApiAuthorizer? {
        return httpApiAuthorizer
    }

    fun referencedFileStorageBucket(origin: String): FileBucketResource? {
        if (origin == "") return null
        for (website in fileBucketWebsites) {
            val websiteSubstitution = "\${${website.annotationBucketName.uppercase(Locale.getDefault())}_URL}"
            if (origin == websiteSubstitution) {
                return website
            }
        }
        return null
    }

    fun getOrCreateRootHttpApi(processingData: ProcessingData): HttpApi {
        if (rootHttpApi == null) {
            val httpApi = HttpApi(nimbusState, stage, processingData)
            resources.addResource(httpApi)

            val httpApiOutput = HttpApiOutput(httpApi, nimbusState)
            outputs.addOutput(httpApiOutput)

            val exportInformation = ExportInformation(
                httpApiOutput.getExportName(),
                "Created HTTP API. Base URL is ",
                "\${NIMBUS_REST_API_URL}")

            val exports = nimbusState.exports.getOrPut(stage) { mutableListOf()}
            exports.add(exportInformation)
            rootHttpApi = httpApi
            resources.addResource(ApiGatewayHttpStage(httpApi, nimbusState))
        }
        return rootHttpApi!!
    }

//    fun getOrCreateRootHttpApiDeployment(processingData: ProcessingData): ApiGatewayHttpDeployment {
//        if (apiGatewayHttpDeployment == null) {
//            apiGatewayHttpDeployment = ApiGatewayHttpDeployment(getOrCreateRootHttpApi(processingData), nimbusState)
//            resources.addResource(apiGatewayHttpDeployment!!)
//            apiGatewayHttpDeployment
//        }
//        return apiGatewayHttpDeployment!!
//    }

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
