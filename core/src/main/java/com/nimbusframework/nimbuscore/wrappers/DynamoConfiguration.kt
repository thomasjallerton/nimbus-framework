package com.nimbusframework.nimbuscore.wrappers

data class DynamoConfiguration(val tableName: String, val readCapacity: Int, val writeCapacity: Int)