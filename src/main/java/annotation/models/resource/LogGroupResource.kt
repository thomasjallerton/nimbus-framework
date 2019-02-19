package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonObject

class LogGroupResource(
        private val className: String,
        private val methodName: String,
        nimbusState: NimbusState
): Resource(nimbusState) {

    override fun getArn(suffix: String): JsonObject {
        val arn = JsonObject()
        arn.addProperty("Fn::Sub", "arn:\${AWS::Partition}:logs:\${AWS::Region}:\${AWS::AccountId}" +
                ":log-group:/aws/lambda/${nimbusState.projectName}-$className-$methodName$suffix")
        return arn
    }

    override fun getName(): String {
        return "$className${methodName}LogGroup"
    }

    override fun toCloudFormation(): JsonObject {
        val logGroupResource = JsonObject()

        logGroupResource.addProperty("Type", "AWS::Logs::LogGroup")

        val properties = getProperties()
        properties.addProperty("LogGroupName", "/aws/lambda/${nimbusState.projectName}-$className-$methodName")

        logGroupResource.add("Properties", properties)

        return logGroupResource
    }

}
