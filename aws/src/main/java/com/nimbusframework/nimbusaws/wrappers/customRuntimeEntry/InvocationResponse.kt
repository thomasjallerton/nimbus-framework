package com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry

data class InvocationResponse(
    var requestId: String,
    var event: String,
    var endpoint: String
)
