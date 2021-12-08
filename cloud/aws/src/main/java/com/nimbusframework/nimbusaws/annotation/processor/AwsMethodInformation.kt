package com.nimbusframework.nimbusaws.annotation.processor

data class AwsMethodInformation(
    val packageName: String = "",
    val generatedClassName: String = "",
    val qualifiedInputName: String = "",
    val qualifiedReturnName: String = ""
)
