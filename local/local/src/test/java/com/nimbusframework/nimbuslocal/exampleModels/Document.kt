package com.nimbusframework.nimbuslocal.exampleModels

import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key

@DocumentStoreDefinition
data class Document(
        @Key
        val name: String = "",
        @Attribute
        val people: List<Person>? = null,
        @Attribute
        val number: Int = 0
)