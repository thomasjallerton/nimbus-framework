package annotation.models.resource

import annotation.models.persisted.NimbusState
import org.json.JSONArray
import org.json.JSONObject

class Policy(
        private val name: String,
        private val nimbusState: NimbusState
){

    private val statements: MutableMap<String, Statement> = mutableMapOf()

    fun toJson(): JSONObject {
        val policy = JSONObject()
        policy.put("PolicyName", "${nimbusState.projectName}-$name-policy")

        val statementsJson = JSONArray()
        for (statement in statements.values) {
            statementsJson.put(statement.toJson())
        }
        val policyDocument = JSONObject()
        policyDocument.put("Version", "2012-10-17")
        policyDocument.put("Statement", statementsJson)

        policy.put("PolicyDocument", policyDocument)

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



    private inner class Statement(
            private val effect: String,
            private val action: String
    ) {
        private val resources: MutableList<Pair<Resource, String>> = mutableListOf()

        fun addResource(resource: Resource, suffix: String) {
            resources.add(Pair(resource, suffix))
        }

        fun toJson(): JSONObject {
            val statement = JSONObject()

            statement.put("Effect", effect)

            val actionJson = JSONArray()
            actionJson.put(action)
            statement.put("Action", actionJson)

            val resourceJson = JSONArray()

            for ((resource, suffix) in resources) {
                resourceJson.put(resource.getArn(suffix))
            }
            statement.put("Resource", resourceJson)

            return statement
        }
    }
}