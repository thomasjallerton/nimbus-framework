package com.nimbusframework.nimbuslocal.exampleModels

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute

@KeyValueStoreDefinition(keyType = Int::class)
data class KeyValue (
        @Attribute
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
)