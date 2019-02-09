package annotation.models.resource.function

import annotation.models.persisted.NimbusState
import annotation.models.processing.MethodInformation
import annotation.models.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class FunctionResource(
        private val handler: String,
        private val methodInformation: MethodInformation,
        private val functionConfig: FunctionConfig,
        nimbusState: NimbusState
) : Resource(nimbusState) {

    private val envVariables: MutableMap<String, String> = mutableMapOf()
    private val jsonEnvVariables: MutableMap<String, JsonObject> = mutableMapOf()


    override fun getName(): String {
        return "${methodInformation.className}${methodInformation.methodName}Function"
    }

    override fun toCloudFormation(): JsonObject {
        val functionResource = JsonObject()
        functionResource.addProperty("Type", "AWS::Lambda::Function")

        val properties = JsonObject()
        val code = JsonObject()
        val s3Bucket = JsonObject()
        s3Bucket.addProperty("Ref", "NimbusDeploymentBucket")

        code.add("S3Bucket", s3Bucket)
        code.addProperty("S3Key", "nimbus/${nimbusState.projectName}/${nimbusState.compilationTimeStamp}/lambdacode")

        properties.add("Code", code)
        properties.addProperty("FunctionName", "${nimbusState.projectName}-${methodInformation.className}-${methodInformation.methodName}")
        properties.addProperty("Handler", handler)
        properties.addProperty("MemorySize", functionConfig.memory)

        val role = JsonObject()
        val roleFunc = JsonArray()
        roleFunc.add("IamRoleLambdaExecution")
        roleFunc.add("Arn")
        role.add("Fn::GetAtt", roleFunc)
        properties.add("Role", role)

        properties.addProperty("Runtime", "java8")
        properties.addProperty("Timeout", functionConfig.timeout)

        val environment = JsonObject()
        val variables = JsonObject()
        for ((key, value) in envVariables) {
            variables.addProperty(key, value)
        }
        for ((key, json) in jsonEnvVariables) {
            variables.add(key, json)
        }
        environment.add("Variables", variables)
        properties.add("Environment", environment)

        val dependsOn = JsonArray()
        dependsOn.add("IamRoleLambdaExecution")
        functionResource.add("DependsOn", dependsOn)

        functionResource.add("Properties", properties)

        return functionResource
    }

    fun addEnvVariable(key: String, value: String) {
        envVariables[key] = value
    }

    fun addEnvVariable(key: String, value: JsonObject) {
        jsonEnvVariables[key] = value
    }

}