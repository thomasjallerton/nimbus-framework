package annotation.models.resource.queue

import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import com.google.gson.JsonObject

class QueueResource(
        nimbusState: NimbusState
): Resource(nimbusState) {
    override fun toCloudFormation(): JsonObject {
        val queue = JsonObject()
        queue.addProperty("Type", "AWS::SQS::Queue")
        return queue
    }

    override fun getName(): String {
        return "SQSQueue"
    }

}

//{
//    "Type" : "AWS::SQS::Queue",
//    "Properties" : {
//    "ContentBasedDeduplication" : Boolean,
//    "DelaySeconds": Integer,
//    "FifoQueue" : Boolean,
//    "KmsMasterKeyId": String,
//    "KmsDataKeyReusePeriodSeconds": Integer,
//    "MaximumMessageSize": Integer,
//    "MessageRetentionPeriod": Integer,
//    "QueueName": String,
//    "ReceiveMessageWaitTimeSeconds": Integer,
//    "RedrivePolicy": RedrivePolicy,
//    "Tags" : [ Resource Tag, ... ],
//    "VisibilityTimeout": Integer
//}
