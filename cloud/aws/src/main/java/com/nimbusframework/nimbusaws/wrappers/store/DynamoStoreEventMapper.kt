package com.nimbusframework.nimbusaws.wrappers.store

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.nimbusframework.nimbuscore.eventabstractions.StoreEvent

object DynamoStoreEventMapper {

    @JvmStatic
    fun getStoreEvent(record: DynamodbEvent.DynamodbStreamRecord, requestId: String): StoreEvent {
        return StoreEvent(
                record.eventName,
                record.eventID,
                requestId
        )
    }

}