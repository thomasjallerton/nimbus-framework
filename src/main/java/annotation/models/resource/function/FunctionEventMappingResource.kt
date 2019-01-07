package annotation.models.resource.function

import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import com.google.gson.JsonObject

class FunctionEventMappingResource(
        val source: Resource,
        val batchSize: Int,
        val function: FunctionResource,
        nimbusState: NimbusState
): Resource(nimbusState) {
    override fun toCloudFormation(): JsonObject {
        val eventMapping = JsonObject()
        eventMapping.addProperty("Type", "AWS::Lambda::EventSourceMapping")
        eventMapping.addProperty("DependsOn", "IamRoleLambdaExecution")

        val properties = JsonObject()
        properties.addProperty("BatchSize", batchSize)
        properties.add("EventSourceArn", source.getArn())
        properties.add("FunctionName", function.getArn())
        properties.addProperty("Enabled", "True")

        eventMapping.add("Properties", properties)

        return eventMapping
    }

    override fun getName(): String {
        return "${function.getName()}${source.getName()}"
    }
}

//"InsertToDynamoEventSourceMappingSQSDispatch": {
//    "Type": "AWS::Lambda::EventSourceMapping",
//    "DependsOn": "IamRoleLambdaExecution",
//    "Properties": {
//        "BatchSize": 10,
//        "EventSourceArn": "dispatch",
//        "FunctionName": {
//        "Fn::GetAtt": [
//        "InsertToDynamoLambdaFunction",
//        "Arn"
//        ]
//    },
//        "Enabled": "True"
//    }
//},