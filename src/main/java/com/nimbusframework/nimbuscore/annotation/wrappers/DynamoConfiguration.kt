package com.nimbusframework.nimbuscore.annotation.wrappers

data class DynamoConfiguration(val tableName: String, val readCapacity: Int, val writeCapacity: Int)