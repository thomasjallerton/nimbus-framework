package localDeployment.exampleModels

import com.nimbusframework.nimbuscore.annotation.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Key

@DocumentStore
data class Document(
        @Key
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
)