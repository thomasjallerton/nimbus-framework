package localDeployment.exampleModels

import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Attribute

@KeyValueStore(keyType = Int::class)
data class KeyValue (
        @Attribute
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
)