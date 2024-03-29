package com.nimbusframework.nimbusaws.cloudformation.model.resource

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class Policy(
        private val name: String,
        private val nimbusState: NimbusState
){

    private val statements: MutableMap<String, Statement> = mutableMapOf()


    fun toJson(): JsonObject {
        val policy = JsonObject()
        policy.addProperty("PolicyName", "${nimbusState.projectName}-$name-policy")

        val statementsJson = JsonArray()
        for (statement in statements.values) {
            statementsJson.add(statement.toJson())
        }
        val policyDocument = JsonObject()
        policyDocument.addProperty("Version", "2012-10-17")
        policyDocument.add("Statement", statementsJson)

        policy.add("PolicyDocument", policyDocument)

        return policy
    }

    fun addAllowStatement(action: String, resource: Resource, suffix: String = "") {
        if (statements.containsKey(action)) {
            statements[action]!!.addResource(resource, suffix)
        } else {
            val newStatement = Statement("Allow", action)
            newStatement.addResource(resource, suffix)
            statements[action] = newStatement
        }
    }

    fun allows(action: String, resource: Resource, suffix: String = ""): Boolean {
        if (statements.containsKey(action)) {
            return statements[action]!!.containsResource(resource, suffix)
        }
        return false
    }

    private inner class Statement(
            private val effect: String,
            private val action: String
    ) {
        private val resources: MutableList<Pair<Resource, String>> = mutableListOf()

        fun addResource(resource: Resource, suffix: String) {
            resources.add(Pair(resource, suffix))
        }

        fun containsResource(resource: Resource, suffix: String): Boolean {
            return resources.contains(Pair(resource, suffix))
        }

        fun toJson(): JsonObject {
            val statement = JsonObject()

            statement.addProperty("Effect", effect)

            val actionJson = JsonArray()
            actionJson.add(action)
            statement.add("Action", actionJson)

            val resourceJson = JsonArray()

            for ((resource, suffix) in resources) {
                val arnJson = resource.getArn()
                if (arnJson.has("Arn")) {
                    resourceJson.add(arnJson["Arn"])
                } else {
                    resourceJson.add(resource.getArn(suffix))
                }
            }
            statement.add("Resource", resourceJson)

            return statement
        }
    }
}
