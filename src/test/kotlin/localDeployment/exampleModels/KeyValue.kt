package localDeployment.exampleModels

import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute

@KeyValueStore(keyType = String::class)
data class KeyValue (
        @Attribute
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
)