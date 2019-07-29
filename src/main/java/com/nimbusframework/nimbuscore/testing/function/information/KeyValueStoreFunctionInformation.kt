package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType

data class KeyValueStoreFunctionInformation(
        val tableName: String,
        val eventType: StoreEventType
): FunctionInformation()