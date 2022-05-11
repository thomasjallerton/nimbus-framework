package com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition

class QueueUrlEnvironmentVariable(
    queueDefinition: ParsedQueueDefinition
): NimbusEnvironmentVariable<ParsedQueueDefinition>(queueDefinition) {

    private val key = "NIMBUS_QUEUE_URL_ID_" + toValidEnvironmentVariableKey(queueDefinition.queueId)

    override fun getKey(): String {
        return key
    }
}
