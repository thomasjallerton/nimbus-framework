package localDeployment.exampleModels

import annotation.annotations.keyvalue.KeyValueStore
import annotation.annotations.persistent.Attribute

@KeyValueStore(keyType = Int::class)
data class House (
    @Attribute
    val name: String = "",
    @Attribute
    val residents: List<Person> = listOf()
)