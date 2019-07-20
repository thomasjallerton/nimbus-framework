package localDeployment.webserver.console

import com.nimbusframework.nimbuscore.testing.LocalNimbusDeployment
import localDeployment.exampleModels.Bucket
import localDeployment.exampleModels.BucketTwo
import localDeployment.exampleModels.Document
import localDeployment.exampleModels.KeyValue
import org.junit.Test

class Server {

    @Test
    fun startServer() {
        val localDeployment = LocalNimbusDeployment.getNewInstance(
                Bucket::class.java,
                BucketTwo::class.java,
                Document::class.java,
                KeyValue::class.java)
        localDeployment.startAllServers()
    }
}