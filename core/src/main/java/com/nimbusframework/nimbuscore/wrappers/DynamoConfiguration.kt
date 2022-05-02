package com.nimbusframework.nimbuscore.wrappers

data class DynamoConfiguration(
        val tableName: String,
        val readCapacityUnits: Int = 5,
        val writeCapacityUnits: Int = 5
)
