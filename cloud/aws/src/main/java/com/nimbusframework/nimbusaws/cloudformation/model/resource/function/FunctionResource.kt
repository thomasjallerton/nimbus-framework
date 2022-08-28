package com.nimbusframework.nimbusaws.cloudformation.model.resource.function

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionIdentifier
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.ConstantEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.cloudformation.model.resource.DirectAccessResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.LogGroupResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FunctionResource(
    val fileBuilderMethodInformation: FileBuilderMethodInformation,
    val functionConfig: FunctionConfig,
    val handlerInformation: HandlerInformation,
    nimbusState: NimbusState
) : Resource(nimbusState, functionConfig.stage), DirectAccessResource {

    private val envVariables: MutableMap<String, String> = mutableMapOf()
    private val jsonEnvVariables: MutableMap<String, JsonObject> = mutableMapOf()

    val iamRoleResource: IamRoleResource = IamRoleResource(getShortName(), nimbusState, functionConfig.stage)
    val logGroupResource = LogGroupResource(fileBuilderMethodInformation.className, fileBuilderMethodInformation.methodName, this, nimbusState, functionConfig.stage)

    init {
        envVariables["NIMBUS_STAGE"] = functionConfig.stage
        iamRoleResource.addAllowStatement("logs:CreateLogStream", logGroupResource, ":*")
        iamRoleResource.addAllowStatement("logs:PutLogEvents", logGroupResource, ":*:*")
        dependsOn.add(iamRoleResource.getName())
    }

    fun getIdentifier(): FunctionIdentifier {
        return FunctionIdentifier(fileBuilderMethodInformation.getQualifiedClassName(), fileBuilderMethodInformation.methodName)
    }

    override fun getName(): String {
        val path = fileBuilderMethodInformation.packageName.split(".").mapNotNull { it.firstOrNull() }.joinToString("").take(24)
        val charactersForClassAndMethod = (56 - path.length) / 2
        val className = if (fileBuilderMethodInformation.className.length > charactersForClassAndMethod) {
            fileBuilderMethodInformation.className.take(charactersForClassAndMethod / 2) + fileBuilderMethodInformation.className.takeLast(charactersForClassAndMethod / 2)
        } else {
            fileBuilderMethodInformation.className
        }
        val methodName = if (fileBuilderMethodInformation.methodName.length > charactersForClassAndMethod) {
            fileBuilderMethodInformation.methodName.take(charactersForClassAndMethod / 2) + fileBuilderMethodInformation.methodName.takeLast(charactersForClassAndMethod / 2)
        } else {
            fileBuilderMethodInformation.methodName
        }
        return "$path$className${methodName}Function"
    }

    fun getShortName(): String {
        val path = fileBuilderMethodInformation.packageName.split(".").mapNotNull { it.firstOrNull() }.joinToString("").take(10)
        val charactersForClassAndMethod = (32 - path.length) / 2
        val className = if (fileBuilderMethodInformation.className.length > charactersForClassAndMethod) {
            fileBuilderMethodInformation.className.take(charactersForClassAndMethod / 2) + fileBuilderMethodInformation.className.takeLast(charactersForClassAndMethod / 2)
        } else {
            fileBuilderMethodInformation.className
        }
        val methodName = if (fileBuilderMethodInformation.methodName.length > charactersForClassAndMethod) {
            fileBuilderMethodInformation.methodName.take(charactersForClassAndMethod / 2) + fileBuilderMethodInformation.methodName.takeLast(charactersForClassAndMethod / 2)
        } else {
            fileBuilderMethodInformation.methodName
        }
        return "$path$className$methodName"
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
        properties.addProperty(
            "FunctionName",
            functionName(nimbusState.projectName, fileBuilderMethodInformation.packageName, fileBuilderMethodInformation.className, fileBuilderMethodInformation.methodName, functionConfig.stage)
        )
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
        return functionName(nimbusState.projectName, fileBuilderMethodInformation.packageName, fileBuilderMethodInformation.className, fileBuilderMethodInformation.methodName, functionConfig.stage)
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

    override fun getAdditionalResources(): List<Resource> {
        return listOf(
            iamRoleResource,
            logGroupResource
        )
    }

    companion object {
        fun functionName(projectName: String, packageName: String, className: String, methodName: String, stage: String): String {
            val shortPackage = packageName.split(".").mapNotNull { it.firstOrNull() }.joinToString("").take(5)
            val desiredName = "$projectName-$stage-$shortPackage-$className-$methodName"
            return if (desiredName.length > 64) {
                "${projectName.take(6)}-${stage.take(5)}-$shortPackage-${className.take(22)}-${methodName.take(22)}"
            } else {
                desiredName
            }
        }
    }

}
