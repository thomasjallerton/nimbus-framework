package cloudformation.resource.file

import annotation.annotations.file.FileStorageEventType
import cloudformation.resource.function.FunctionResource
import com.google.gson.JsonObject

class LambdaConfiguration(
        private val eventType: FileStorageEventType,
        private val functionResource: FunctionResource
) {

    fun toJson(): JsonObject {
        val lambdaConfiguration = JsonObject()
        lambdaConfiguration.addProperty("Event", eventTypeToS3EventType())
        lambdaConfiguration.add("Function", functionResource.getArn())

        return lambdaConfiguration
    }

    private fun eventTypeToS3EventType(): String {
        return if (eventType == FileStorageEventType.OBJECT_CREATED) {
            "s3:ObjectCreated:*"
        } else {
            "s3:ObjectRemoved:*"
        }
    }
}