package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class FunctionResource(
        private val handler: String,
        private val className: String,
        private val methodName: String,
        nimbusState: NimbusState
) : Resource(nimbusState) {

    private val timeout: Int = 5
    private val memory: Int = 1024

    override fun getName(): String {
        return "$className${methodName}Function"
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
        properties.addProperty("FunctionName", "${nimbusState.projectName}-$className-$methodName")
        properties.addProperty("Handler", handler)
        properties.addProperty("MemorySize", memory)

        val role = JsonObject()
        val roleFunc = JsonArray()
        roleFunc.add("IamRoleLambdaExecution")
        roleFunc.add("Arn")
        role.add("Fn::GetAtt", roleFunc)
        properties.add("Role", role)

        properties.addProperty("Runtime", "java8")
        properties.addProperty("Timeout", timeout)

        val dependsOn = JsonArray()
        dependsOn.add("IamRoleLambdaExecution")
        functionResource.add("DependsOn", dependsOn)

        functionResource.add("Properties", properties)

        return functionResource
    }

}