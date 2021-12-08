package com.nimbusframework.nimbuslocal.deployment.function.information

import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType

data class DocumentStoreFunctionInformation(
        val tableName: String,
        val eventType: StoreEventType
): FunctionInformation(FunctionType.DOCUMENT_STORE)