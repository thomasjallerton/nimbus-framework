package com.nimbusframework.nimbusaws.examples.document

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key

@DynamoDbDocumentStore(stages = ["dev"])
data class KotlinDocumentStoreNoTableName(
    @Key
    var string: String = "" ,
    @Attribute
    var documents: List<DocumentStoreNoTableName> = listOf()
)
