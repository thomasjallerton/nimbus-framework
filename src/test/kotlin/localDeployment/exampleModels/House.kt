package testpackage

import annotation.annotations.keyvalue.KeyType
import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute

@KeyValueStore(keyType = KeyType.NUMBER)
data class House (
    @Attribute
    val name: String = "",
    @Attribute
    val residents: List<Person> = listOf()
)