package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonObject

class RestApiResource(
        private val parent: AbstractRestResource,
        private val pathPart: String,
        nimbusState: NimbusState
): AbstractRestResource(nimbusState) {
    override fun getPath(): String {
        val fixedPathPart = pathPart.replace("[{}]".toRegex(), "")
        return parent.getPath() + fixedPathPart
    }

    override fun getId(): JsonObject {
        val id = JsonObject()
        id.addProperty("Ref", getName())
        return id
    }

    override fun getRootId(): JsonObject {
        return parent.getRootId()
    }

    override fun getName(): String {
        return "ApiGatewayResource${getPath()}"
    }

    override fun toCloudFormation(): JsonObject {
        val gatewayResource =  JsonObject()
        gatewayResource.addProperty("Type", "AWS::ApiGateway::Resource")

        val properties = JsonObject()
        properties.add("ParentId", parent.getId())
        properties.addProperty("PathPart", pathPart)
        properties.add("RestApiId", parent.getRootId())

        gatewayResource.add("Properties", properties)
        return gatewayResource
    }
}
