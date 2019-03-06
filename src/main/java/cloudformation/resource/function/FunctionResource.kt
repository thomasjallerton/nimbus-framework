package cloudformation.resource.function

import cloudformation.persisted.NimbusState
import cloudformation.processing.MethodInformation
import cloudformation.resource.IamRoleResource
import cloudformation.resource.Resource
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
    private lateinit var iamRoleResource: IamRoleResource

    fun setIamRoleResource(resource: IamRoleResource) {
        iamRoleResource = resource
        dependsOn.add(iamRoleResource.getName())
    }

    fun getIamRoleResource(): IamRoleResource {
        return iamRoleResource
    }

    override fun getName(): String {
        return "${methodInformation.className}${methodInformation.methodName}Function"
    }

    fun getShortName(): String {
        return if (methodInformation.className.length > 5) {
            "${methodInformation.className.substring(0, 5)}${methodInformation.methodName}"
        } else {
            "${methodInformation.className}${methodInformation.methodName}"
        }
    }

    override fun toCloudFormation(): JsonObject {
        val functionResource = JsonObject()
        functionResource.addProperty("Type", "AWS::Lambda::Function")

        val properties = getProperties()
        val code = JsonObject()
        val s3Bucket = JsonObject()
        s3Bucket.addProperty("Ref", "NimbusDeploymentBucket")

        code.add("S3Bucket", s3Bucket)
        code.addProperty("S3Key", "nimbus/${nimbusState.projectName}/${nimbusState.compilationTimeStamp}/lambdacode")

        properties.add("Code", code)
        properties.addProperty("FunctionName", functionName(nimbusState.projectName, methodInformation.className, methodInformation.methodName))
        properties.addProperty("Handler", handler)
        properties.addProperty("MemorySize", functionConfig.memory)

        properties.add("Role", iamRoleResource.getArn())

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

    companion object {
        fun functionName(projectName: String, className: String, methodName: String): String {
            return "$projectName-$className-$methodName"
        }
    }

}