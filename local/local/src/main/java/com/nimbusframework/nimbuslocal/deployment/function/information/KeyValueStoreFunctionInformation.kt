package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class KeyValueStoreFunctionInformation(
        val tableName: String,
        val eventType: StoreEventType
): FunctionInformation(FunctionType.KEY_VALUE_STORE)