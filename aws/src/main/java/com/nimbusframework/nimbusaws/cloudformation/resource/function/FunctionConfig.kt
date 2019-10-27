package com.nimbusframework.nimbusaws.cloudformation.resource.function

data class FunctionConfig(val timeout: Int, val memory: Int, val stage: String)