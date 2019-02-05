package wrappers.queue.models

import wrappers.ServerlessEvent

data class QueueEvent(
        val messageId: String? = null,
        val receiptHandle: String? = null,
        val body: String? = null,
        val attributes: Attributes? = null,
        val messageAttributes: Map<String, MessageAttribute>? = null,
        val md5OfMessageAttributes: String? = null,
        val md5OfBody: String? = null,
        val eventSource: String? = null,
        val eventSourceARN: String? = null,
        val awsRegion: String? = null
): ServerlessEvent
