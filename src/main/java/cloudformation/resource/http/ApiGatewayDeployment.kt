package cloudformation.resource.http

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject
import java.util.*

class ApiGatewayDeployment(
        private val restApi: RestApi,
        nimbusState: NimbusState
):Resource(nimbusState, restApi.stage) {

    override fun toCloudFormation(): JsonObject {
        val deployment = JsonObject()
        deployment.addProperty("Type", "AWS::ApiGateway::Deployment")
        val properties = getProperties()
        properties.addProperty("Description", "Nimbus Deployment for project ${nimbusState.projectName}")
        properties.add("RestApiId", restApi.getRef())
        properties.addProperty("StageName", "dev")
        deployment.add("Properties", properties)
        deployment.add("DependsOn", dependsOn)
        return deployment
    }

    override fun getName(): String {
        val calendar = Calendar.getInstance()
        return "${nimbusState.projectName}ApiGatewayDeployment${calendar.timeInMillis}"
    }
}