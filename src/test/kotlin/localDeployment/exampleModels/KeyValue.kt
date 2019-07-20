package localDeployment.exampleModels

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment
import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.KeyValueStore
import com.nimbusframework.nimbuscore.annotation.annotations.keyvalue.UsesKeyValueStore
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment

@KeyValueStore(keyType = Int::class)
data class KeyValue (
        @Attribute
        val name: String = "",
        @Attribute
        val people: List<Person> = listOf()
) {

        @AfterDeployment
        @UsesKeyValueStore(dataModel = KeyValue::class)
        fun insertItem() {
                val client = ClientBuilder.getKeyValueStoreClient(Int::class.java, KeyValue::class.java)
                client.put(15, KeyValue("test"))
        }

}