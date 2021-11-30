package com.nimbusframework.nimbusaws.clients.dynamo

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object DynamoHelper {

    fun strAttribute(string: String): AttributeValue {
        return AttributeValue.builder().s(string).build()
    }

    fun numAttribute(num: String): AttributeValue {
        return AttributeValue.builder().n(num).build()
    }

}
