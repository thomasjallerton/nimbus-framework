package localDeployment.exampleModels

import annotation.annotations.document.DocumentStore
import annotation.annotations.persistent.Attribute
import annotation.annotations.persistent.Key
import testpackage.Person

@DocumentStore
data class Document(
        @Key
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
)