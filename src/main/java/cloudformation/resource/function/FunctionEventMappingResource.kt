package cloudformation.resource.function

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class FunctionEventMappingResource(
        private val source: JsonObject,
        private val sourceName: String,
        private val batchSize: Int,
        val function: FunctionResource,
        private val includeStartingPosition: Boolean,
        nimbusState: NimbusState
): Resource(nimbusState, function.stage) {
    override fun toCloudFormation(): JsonObject {
        val eventMapping = JsonObject()
        eventMapping.addProperty("Type", "AWS::Lambda::EventSourceMapping")
        eventMapping.addProperty("DependsOn", function.getIamRoleResource().getName())

        val properties = getProperties()
        properties.addProperty("BatchSize", batchSize)
        properties.add("EventSourceArn", source)
        properties.add("FunctionName", function.getArn())
        properties.addProperty("Enabled", "True")

        if (includeStartingPosition) {
            properties.addProperty("StartingPosition", "LATEST")
        }

        eventMapping.add("Properties", properties)

        return eventMapping
    }

    override fun getName(): String {
        return "${function.getShortName()}$sourceName"
    }
}