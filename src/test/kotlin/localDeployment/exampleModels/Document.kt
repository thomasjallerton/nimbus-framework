package localDeployment.exampleModels

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotation.annotations.document.DocumentStore
import com.nimbusframework.nimbuscore.annotation.annotations.document.UsesDocumentStore
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Key
import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment

@DocumentStore
data class Document(
        @Key
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
) {

        @AfterDeployment
        @UsesDocumentStore(dataModel = Document::class)
        fun addItem() {
                ClientBuilder.getDocumentStoreClient(Document::class.java).put(Document("test"))
        }
}