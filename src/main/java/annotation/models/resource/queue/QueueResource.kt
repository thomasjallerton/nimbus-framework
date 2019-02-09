package annotation.models.resource.queue

import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import com.google.gson.JsonObject

class QueueResource(
        nimbusState: NimbusState,
        private val name: String
): Resource(nimbusState) {
    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")
        return queue
    }

    override fun getName(): String {
        return "NimbusSQSQueue$name"
    }
}
