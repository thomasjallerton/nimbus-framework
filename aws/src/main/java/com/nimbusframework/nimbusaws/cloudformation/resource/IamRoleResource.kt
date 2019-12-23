package com.nimbusframework.nimbusaws.cloudformation.resource

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class IamRoleResource(
        private val functionName: String,
        nimbusState: NimbusState,
        stage: String
) : Resource(nimbusState, stage) {

    private val policy: Policy = Policy(functionName, nimbusState)

    override fun toCloudFormation(): JsonObject {
        val iamRoleResource = JsonObject()

        iamRoleResource.addProperty("Type", "AWS::IAM::Role")

        val properties = getProperties()
        properties.add("AssumeRolePolicyDocument", rolePolicyDocument())

        val policies = JsonArray()
        policies.add(policy.toJson())
        properties.add("Policies", policies)

        properties.addProperty("Path", "/")

        val projectName = if (nimbusState.projectName.length  < 15) {
            nimbusState.projectName
        } else {
            nimbusState.projectName.substring(0..15)
        }

        properties.addProperty("RoleName", "$projectName-$stage-$functionName")

        iamRoleResource.add("Properties", properties)

        return iamRoleResource
    }

    override fun getName(): String {
        return "IamRole${functionName}Execution"
    }

    fun addAllowStatement(action: String, resource: Resource, suffix: String) {
        policy.addAllowStatement(action, resource, suffix)
    }

    fun allows(action: String, resource: Resource, suffix: String = ""): Boolean {
        return policy.allows(action, resource, suffix)
    }

    private fun rolePolicyDocument(): JsonObject {
        val rolePolicyDocument = JsonObject()
        rolePolicyDocument.addProperty("Version", "2012-10-17")

        val statement = JsonObject()
        statement.addProperty("Effect", "Allow")
        val principal = JsonObject()
        val service = JsonArray()
        service.add("lambda.amazonaws.com")
        principal.add("Service", service)
        statement.add("Principal", principal)
        val action = JsonArray()
        action.add("sts:AssumeRole")
        statement.add("Action", action)

        val statements = JsonArray()
        statements.add(statement)

        rolePolicyDocument.add("Statement", statements)

        return rolePolicyDocument
    }

}