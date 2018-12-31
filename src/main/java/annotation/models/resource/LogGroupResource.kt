package annotation.models.resource

import org.json.JSONObject

class LogGroupResource(
        private val name: String
): Resource() {

    override fun getArn(suffix: String): JSONObject {
        val arn = JSONObject()
        arn.put("Fn::Sub", "arn:\${AWS::Partition}:logs:\${AWS::Region}:\${AWS::AccountId}:log-group:/aws/lambda/$name$suffix")
        return arn
    }

    override fun getName(): String {
        return "${name}LogGroup"
    }

    override fun toCloudFormation(): JSONObject {
        val logGroupResource = JSONObject()

        logGroupResource.put("Type", "AWS::Logs::LogGroup")

        val properties = JSONObject()
        properties.put("LogGroupName", "/aws/lambda/$name")

        logGroupResource.put("Properties", properties)

        return logGroupResource
    }

}
