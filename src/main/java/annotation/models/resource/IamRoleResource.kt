package annotation.models.resource

import org.json.JSONArray
import org.json.JSONObject

class IamRoleResource(
        private val policy: Policy
): Resource {
    override fun getArn(suffix: String): JSONObject {
        val arn = JSONObject()
        val list = JSONArray()
        list.put("IamRoleLambdaExecution")
        list.put("Arn")
        arn.put("Fn::GetAtt", list)
        return arn
    }

    override fun toCloudFormation(): JSONObject {
        val iamRoleResource = JSONObject()

        iamRoleResource.put("Type", "AWS::IAM::Role")

        val properties = JSONObject()
        properties.put("AssumeRolePolicyDocument", rolePolicyDocument())

        val policies = JSONArray()
        policies.put(policy.toJson())
        properties.put("Policies", policies)

        properties.put("Path", "/")

        properties.put("RoleName", "project-stage-region-lambdaRole")

        iamRoleResource.put("Properties", properties)

        return iamRoleResource
    }

    override fun getName(): String {
        return "IamRoleLambdaExecution"
    }

    private fun rolePolicyDocument(): JSONObject {
        val rolePolicyDocument = JSONObject()
        rolePolicyDocument.put("Version", "2012-10-17")

        val statement = JSONObject()
        statement.put("Effect", "Allow")
        val principal = JSONObject()
        val service = JSONArray()
        service.put("lambda.amazonaws.com")
        principal.put("Service", service)
        statement.put("Principal", principal)
        val action = JSONArray()
        action.put("sts:AssumeRole")
        statement.put("Action", action)

        val statements = JSONArray()
        statements.put(statement)

        rolePolicyDocument.put("Statement", statements)

        return rolePolicyDocument
    }

}