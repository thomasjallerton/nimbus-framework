package com.nimbusframework.nimbuscore.cloudformation

import com.nimbusframework.nimbuscore.cloudformation.outputs.OutputCollection
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.cloudformation.resource.ResourceCollection
import com.nimbusframework.nimbuscore.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbuscore.cloudformation.resource.http.ApiGatewayDeployment
import com.nimbusframework.nimbuscore.cloudformation.resource.http.RestApi
import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.WebSocketApi
import com.nimbusframework.nimbuscore.cloudformation.resource.websocket.WebSocketDeployment

data class CloudFormationDocuments(
        val createResources: ResourceCollection = ResourceCollection(),
        val updateResources: ResourceCollection = ResourceCollection(),
        val createOutputs: OutputCollection = OutputCollection(),
        val updateOutputs: OutputCollection = OutputCollection(),
        val savedResources: MutableMap<String, Resource> = mutableMapOf(),

        val fileBucketWebsites: MutableList<FileBucket> = mutableListOf(),
        var rootRestApi: RestApi? = null,
        var apiGatewayDeployment: ApiGatewayDeployment? = null,
        var rootWebSocketApi: WebSocketApi? = null,
        var webSocketDeployment: WebSocketDeployment? = null
) {

    fun referencedFileStorageBucket(origin: String): FileBucket? {
        if (origin == "") return null
        for (website in fileBucketWebsites) {
            val websiteSubstitution = "\${${website.annotationBucketName.toUpperCase()}_URL}"
            println(websiteSubstitution)
            if (origin == websiteSubstitution) {
                return website
            }
        }
        return null
    }
}