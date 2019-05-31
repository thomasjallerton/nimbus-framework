package com.nimbusframework.nimbuscore.wrappers.store.models

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusframework.nimbuscore.wrappers.ServerlessEvent
import java.util.*

data class StoreEvent(
        val eventName: String? = "",
        val eventId: String? = "",
        val requestId: String = UUID.randomUUID().toString()
): ServerlessEvent {

        constructor(record: DynamodbEvent.DynamodbStreamRecord, requestId: String):
                this (
                        record.eventName,
                        record.eventID,
                        requestId
                )

}