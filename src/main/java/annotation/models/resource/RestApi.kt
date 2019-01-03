package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class RestApi(
    nimbusState: NimbusState
): AbstractRestResource(nimbusState) {
    override fun getPath(): String {
        return ""
    }

    override fun getId(): JsonObject {
        val parentId = JsonObject()
        val getAttr = JsonArray()
        getAttr.add("ApiGatewayRestApi")
        getAttr.add("RootResourceId")
        parentId.add("Fn::GetAtt", getAttr)
        return parentId
    }

    override fun getRootId(): JsonObject {
        val rootId = JsonObject()
        rootId.addProperty("Ref", getName())
        return rootId
    }

    override fun toCloudFormation(): JsonObject {
        val restApi = JsonObject()
        restApi.addProperty("Type", "AWS::ApiGateway::RestApi")

        val properties = JsonObject()
        properties.addProperty("Name", nimbusState.projectName)

        val endpointConfig = JsonObject()
        val types = JsonArray()
        types.add("EDGE")

        endpointConfig.add("Types", types)

        properties.add("EndpointConfiguration", endpointConfig)

        restApi.add("Properties", properties)

        return restApi
    }

    override fun getName(): String {
        return "ApiGatewayRestApi"
    }

    override fun getArn(suffix: String): JsonObject {
        val arn = JsonObject()
        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add("arn:")

        val partitionRef = JsonObject()
        partitionRef.addProperty("Ref", "AWS::Partition")
        join.add(partitionRef)

        join.add(":execute-api:")

        val regionRef = JsonObject()
        regionRef.addProperty("Ref", "AWS::Region")
        join.add(regionRef)

        join.add(":")

        val accountIdRef = JsonObject()
        accountIdRef.addProperty("Ref", "AWS::AccountId")
        join.add(accountIdRef)

        join.add(":")

        join.add(getRootId())

        join.add(suffix)

        joinFunc.add(join)

        arn.add("Fn::Join", joinFunc)

        return arn
    }

}