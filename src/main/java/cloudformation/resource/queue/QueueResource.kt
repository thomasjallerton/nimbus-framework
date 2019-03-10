package cloudformation.resource.queue

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

class QueueResource(
        nimbusState: NimbusState,
        private val name: String,
        private val visibilityTimeout: Int,
        stage: String
): Resource(nimbusState, stage) {
    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")

        val properties = getProperties()
        properties.addProperty("VisibilityTimeout", visibilityTimeout)

        queue.add("Properties", properties)

        return queue
    }

    override fun getName(): String {
        return "NimbusSQSQueue$name"
    }
}
