package cloudformation

import cloudformation.outputs.OutputCollection
import cloudformation.resource.Resource
import cloudformation.resource.ResourceCollection
import cloudformation.resource.http.ApiGatewayDeployment
import cloudformation.resource.http.RestApi

data class CloudFormationDocuments(
        val createResources: ResourceCollection = ResourceCollection(),
        val updateResources: ResourceCollection = ResourceCollection(),
        val createOutputs: OutputCollection = OutputCollection(),
        val updateOutputs: OutputCollection = OutputCollection(),
        val savedResources: MutableMap<String, Resource> = mutableMapOf(),
        var rootRestApi: RestApi? = null,
        var apiGatewayDeployment: ApiGatewayDeployment? = null
)