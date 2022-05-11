package com.nimbusframework.nimbusaws.cloudformation.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.BucketNameOutput
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.OutputCollection
import com.nimbusframework.nimbusaws.cloudformation.model.outputs.RestApiOutput
import com.nimbusframework.nimbusaws.cloudformation.model.resource.NimbusBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ResourceCollection
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.ApiGatewayDeployment
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.RestApi
import com.nimbusframework.nimbusaws.cloudformation.model.resource.http.authorizer.RestApiAuthorizer
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
    private var rootRestApi: RestApi? = null,
    private var restApiAuthorizer: RestApiAuthorizer? = null,
    private var apiGatewayDeployment: ApiGatewayDeployment? = null,
    var rootWebSocketApi: WebSocketApi? = null,
    var webSocketDeployment: WebSocketDeployment? = null

) {

    init {
        val bucket = NimbusBucketResource(nimbusState, stage)
        val bucketNameOutput = BucketNameOutput(bucket, nimbusState)
        resources.addResource(bucket)
        outputs.addOutput(bucketNameOutput)
    }

    fun addRestApiAuthorizer(authorizer: RestApiAuthorizer) {
        restApiAuthorizer = authorizer
        resources.addResource(authorizer)
    }

    fun getRestApiAuthorizer(): RestApiAuthorizer? {
        return restApiAuthorizer
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

    fun getOrCreateRootRestApi(): RestApi {
        if (rootRestApi == null) {
            val restApi = RestApi(nimbusState, stage)
            resources.addResource(restApi)

            val httpApiOutput = RestApiOutput(restApi, nimbusState)
            outputs.addOutput(httpApiOutput)

            val exportInformation = ExportInformation(
                httpApiOutput.getExportName(),
                "Created REST API. Base URL is ",
                "\${NIMBUS_REST_API_URL}")

            val exports = nimbusState.exports.getOrPut(stage) { mutableListOf()}
            exports.add(exportInformation)
            rootRestApi = restApi
        }
        return rootRestApi!!
    }

    fun getOrCreateRootRestApiDeployment(): ApiGatewayDeployment {
        if (apiGatewayDeployment == null) {
            apiGatewayDeployment = ApiGatewayDeployment(getOrCreateRootRestApi(), nimbusState)
            resources.addResource(apiGatewayDeployment!!)
            apiGatewayDeployment
        }
        return apiGatewayDeployment!!
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
