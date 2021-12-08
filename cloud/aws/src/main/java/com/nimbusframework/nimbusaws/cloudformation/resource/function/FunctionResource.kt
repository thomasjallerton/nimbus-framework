package com.nimbusframework.nimbusaws.cloudformation.resource.function

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FunctionResource(
        private val handler: String,
        private val methodInformation: MethodInformation,
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

    fun addClient(client: ClientType) {
        handlerInformation.usesClients.add(client)
    }

    fun usesClient(client: ClientType): Boolean {
        return handlerInformation.usesClients.contains(client)
    }

    fun addExtraDependency(classPath: String) {
        handlerInformation.extraDependencies.add(classPath)
    }

    fun containsDependency(classPath: String): Boolean {
        return handlerInformation.extraDependencies.contains(classPath)
    }

    override fun getName(): String {
        val className = if (methodInformation.className.length > 28) {
            methodInformation.className.takeLast(28)
        } else {
            methodInformation.className
        }
        val methodName = if (methodInformation.className.length > 28) {
            methodInformation.methodName.takeLast(28)
        } else {
            methodInformation.methodName
        }
        return "$className${methodName}Function"
    }

    fun getShortName(): String {
        val className = if (methodInformation.className.length > 16) {
            methodInformation.className.takeLast(16)
        } else {
            methodInformation.className
        }
        val methodName = if (methodInformation.className.length > 16) {
            methodInformation.methodName.takeLast(16)
        } else {
            methodInformation.methodName
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
            code.addProperty("S3Key", "nimbus/${nimbusState.projectName}/${handlerInformation.replacementVariable}")

        val (runtime, finalHandler) = if (nimbusState.customRuntime) {
            Pair("provided", "provided")
        } else {
            Pair("java11", handler)
        }

        properties.add("Code", code)
        properties.addProperty("FunctionName", functionName(nimbusState.projectName, methodInformation.className, methodInformation.methodName, functionConfig.stage))
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

    fun addEnvVariable(key: String, value: String) {
        envVariables[key] = value
    }

    fun addEnvVariable(key: String, value: JsonObject) {
        jsonEnvVariables[key] = value
    }

    fun getStrEnvValue(key: String): String? {
        return envVariables[key]
    }

    fun getJsonEnvValue(key: String): JsonObject? {
        return jsonEnvVariables[key]
    }

    fun getFunctionName(): String {
        return functionName(nimbusState.projectName, methodInformation.className, methodInformation.methodName, functionConfig.stage)
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
