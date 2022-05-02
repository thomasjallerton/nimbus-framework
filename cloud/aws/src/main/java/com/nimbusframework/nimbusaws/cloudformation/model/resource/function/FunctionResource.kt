package com.nimbusframework.nimbusaws.cloudformation.model.resource.function

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FunctionResource(
    private val fileBuilderMethodInformation: FileBuilderMethodInformation,
    private val functionConfig: FunctionConfig,
    private val handlerInformation: HandlerInformation,
    nimbusState: NimbusState
) : Resource(nimbusState, functionConfig.stage) {

    private val envVariables: MutableMap<String, String> = mutableMapOf()
    private val jsonEnvVariables: MutableMap<String, JsonObject> = mutableMapOf()
    private lateinit var iamRoleResource: IamRoleResource

    init {
        envVariables["NIMBUS_STAGE"] = functionConfig.stage
    }

    fun setIamRoleResource(resource: IamRoleResource) {
        iamRoleResource = resource
        dependsOn.add(iamRoleResource.getName())
    }

    fun getIamRoleResource(): IamRoleResource {
        return iamRoleResource
    }

    override fun getName(): String {
        val className = if (fileBuilderMethodInformation.className.length > 28) {
            fileBuilderMethodInformation.className.takeLast(28)
        } else {
            fileBuilderMethodInformation.className
        }
        val methodName = if (fileBuilderMethodInformation.className.length > 28) {
            fileBuilderMethodInformation.methodName.takeLast(28)
        } else {
            fileBuilderMethodInformation.methodName
        }
        return "$className${methodName}Function"
    }

    fun getShortName(): String {
        val className = if (fileBuilderMethodInformation.className.length > 16) {
            fileBuilderMethodInformation.className.takeLast(16)
        } else {
            fileBuilderMethodInformation.className
        }
        val methodName = if (fileBuilderMethodInformation.className.length > 16) {
            fileBuilderMethodInformation.methodName.takeLast(16)
        } else {
            fileBuilderMethodInformation.methodName
        }
        return "$className$methodName"
    }

    override fun toCloudFormation(): JsonObject {
        val functionResource = JsonObject()
        functionResource.addProperty("Type", "AWS::Lambda::Function")

        val properties = getProperties()
        val code = JsonObject()
        val s3Bucket = JsonObject()
        s3Bucket.addProperty("Ref", "NimbusDeploymentBucket")

        code.add("S3Bucket", s3Bucket)
            code.addProperty("S3Key", "nimbus/${nimbusState.projectName}/${handlerInformation.fileReplacementVariable}")

        val (runtime, finalHandler) = if (nimbusState.customRuntime) {
            Pair("provided", "provided")
        } else {
            Pair(handlerInformation.runtime, handlerInformation.handlerPath)
        }

        properties.add("Code", code)
        properties.addProperty("FunctionName", functionName(nimbusState.projectName, fileBuilderMethodInformation.className, fileBuilderMethodInformation.methodName, functionConfig.stage))
        properties.addProperty("Handler", finalHandler)
        properties.addProperty("MemorySize", functionConfig.memory)

        properties.add("Role", iamRoleResource.getArn())
        properties.addProperty("Runtime", runtime)
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

    fun addEnvVariable(key: NimbusEnvironmentVariable<*>, value: String) {
        envVariables[key.getKey()] = value
    }

    fun addEnvVariable(key: NimbusEnvironmentVariable<*>, value: JsonObject) {
        jsonEnvVariables[key.getKey()] = value
    }

    fun addEnvVariable(key: ConstantEnvironmentVariable, value: String) {
        envVariables[key.name] = value
    }

    fun addEnvVariable(key: ConstantEnvironmentVariable, value: JsonObject) {
        jsonEnvVariables[key.name] = value
    }

    fun getStrEnvValue(key: String): String? {
        return envVariables[key]
    }

    fun getJsonEnvValue(key: String): JsonObject? {
        return jsonEnvVariables[key]
    }

    fun getFunctionName(): String {
        return functionName(nimbusState.projectName, fileBuilderMethodInformation.className, fileBuilderMethodInformation.methodName, functionConfig.stage)
    }

    fun getUri(): JsonObject {
        val uri = JsonObject()
        val joinFunc = JsonArray()
        joinFunc.add("")

        val join = JsonArray()
        join.add("arn:")

        val partitionRef = JsonObject()
        partitionRef.addProperty("Ref", "AWS::Partition")
        join.add(partitionRef)

        join.add(":apigateway:")

        val regionRef = JsonObject()
        regionRef.addProperty("Ref", "AWS::Region")
        join.add(regionRef)

        join.add(":lambda:path/2015-03-31/functions/")

        join.add(getArn(""))

        join.add("/invocations")

        joinFunc.add(join)

        uri.add("Fn::Join", joinFunc)
        return uri
    }

    companion object {
        fun functionName(projectName: String, className: String, methodName: String, stage: String): String {
            val desiredName = "$projectName-$stage-$className-$methodName"
            return if (desiredName.length > 64) {
                "${projectName.take(15)}-${stage.take(15)}-${className.take(15)}-${methodName.take(15)}"
            } else {
                desiredName
            }
        }
    }

}
