package com.nimbusframework.nimbuscore.testing.function.information

import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuscore.testing.function.FunctionType

data class KeyValueStoreFunctionInformation(
        val tableName: String,
        val eventType: StoreEventType
): FunctionInformation(FunctionType.KEY_VALUE_STORE)