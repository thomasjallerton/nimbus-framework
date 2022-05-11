package com.nimbusframework.nimbusaws.annotation.annotations.parsed

import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition

class ParsedQueueDefinition(
    val queueId: String,
    val itemProcessingTimeout: Int,
    val stages: Array<String> = arrayOf()
): ParsedAnnotation {

    constructor(queue: QueueDefinition): this(
        queue.queueId,
        queue.itemProcessingTimeout,
        queue.stages
    )

}
