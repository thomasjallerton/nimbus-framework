package com.nimbusframework.nimbusaws.cloudformation.model.resource

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionIdentifier
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.dynamo.DynamoResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.file.FileBucketResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.notification.SnsTopicResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition

class ResourceCollection {

    private val resourceMap: MutableMap<String, Resource> = mutableMapOf()
    private val invokableFunctions: MutableMap<FunctionIdentifier, Resource> = mutableMapOf()

    private val queues: MutableMap<String, QueueResource> = mutableMapOf()
    private val dynamoStores: MutableMap<String, DynamoResource> = mutableMapOf()
    private val databases: MutableMap<String, RdsResource> = mutableMapOf()
    private val snsTopics: MutableMap<String, SnsTopicResource> = mutableMapOf()
    private val s3Buckets: MutableMap<String, FileBucketResource> = mutableMapOf()
    private val lambdaResources: MutableMap<FunctionIdentifier, FunctionResource> = mutableMapOf()
    private val cognitoResource: MutableMap<String, ExistingResource> = mutableMapOf()

    fun addResource(resource: Resource) {
        if (resource is DirectAccessResource) {
            error("Do not add a direct access resource this way (${resource}), instead add via the dedicated method")
        }
        internalAddResource(resource)
    }

    private fun internalAddResource(resource: Resource) {
        if (!resourceMap.containsKey(resource.getName())) {
            resourceMap[resource.getName()] = resource
        }
    }

    fun addFunction(function: FunctionResource) {
        lambdaResources[function.getIdentifier()] = function
        internalAddResource(function)
    }

    fun getFunction(functionIdentifier: FunctionIdentifier): FunctionResource? {
        return lambdaResources[functionIdentifier]
    }

    fun getFunction(qualifiedClassName: String, methodName: String): FunctionResource? {
        return lambdaResources[FunctionIdentifier(qualifiedClassName, methodName)]
    }

    fun addQueue(queueResource: QueueResource) {
        queues[queueResource.definition.queueId] = queueResource
        internalAddResource(queueResource)
    }

    fun getQueue(queueDefinition: QueueDefinition): QueueResource? {
        return queues[queueDefinition.queueId]
    }

    fun addDynamoResource(dataClassSimpleName: String, resource: DynamoResource) {
        dynamoStores[dataClassSimpleName] = resource
        internalAddResource(resource)
    }

    fun addCognitoResource(simpleClassName: String, resource: ExistingResource) {
        cognitoResource[simpleClassName] = resource
    }

    fun getCognitoResource(simpleClassName: String): ExistingResource? {
        return cognitoResource[simpleClassName]
    }

    fun getDynamoResource(dataClassSimpleName: String): DynamoResource? {
        return dynamoStores[dataClassSimpleName]
    }

    fun addDatabase(rdsResource: RdsResource) {
        databases[rdsResource.parsedDatabaseConfig.name] = rdsResource
        internalAddResource(rdsResource)
    }

    fun getDatabase(relationalDatabaseDefinition: RelationalDatabaseDefinition): RdsResource? {
        return databases[relationalDatabaseDefinition.name]
    }

    fun getDatabase(relationalDatabaseDefinition: RdsDatabase): RdsResource? {
        return databases[relationalDatabaseDefinition.name]
    }

    fun addSnsResource(snsTopicResource: SnsTopicResource) {
        snsTopics[snsTopicResource.annotation.topicName] = snsTopicResource
        internalAddResource(snsTopicResource)
    }

    fun getSnsResource(notificationTopicDefinition: NotificationTopicDefinition): SnsTopicResource? {
        return snsTopics[notificationTopicDefinition.topicName]
    }

    fun addFileStorageBucket(fileBucketResource: FileBucketResource) {
        s3Buckets[fileBucketResource.annotationBucketName] = fileBucketResource
        internalAddResource(fileBucketResource)
    }

    fun getFileStorageBucket(fileStorageBucketDefinition: FileStorageBucketDefinition): FileBucketResource? {
        return s3Buckets[fileStorageBucketDefinition.bucketName]
    }

    fun get(id: String): Resource? {
        return resourceMap[id]
    }

    fun find(condition: (Resource) -> Boolean): Resource? {
        return resourceMap.values.firstOrNull(condition)
    }

    fun isEmpty(): Boolean {
        return resourceMap.isEmpty()
    }

    fun size(): Int {
        return resourceMap.size
    }

    fun toJson(): JsonObject {
        val resources = JsonObject()

        for (resource in resourceMap.values) {
            processResource(resources, resource)
        }

        return resources
    }

    private fun processResource(resources: JsonObject, resource: Resource) {
        resource.getAdditionalResources().forEach {
            processResource(resources, it)
        }
        resources.add(resource.getName(), resource.toCloudFormation())
    }

    fun contains(resource: Resource): Boolean {
        return resourceMap.containsKey(resource.getName())
    }

    fun getInvokableFunction(className: String, methodName: String): Resource? {
        return invokableFunctions[FunctionIdentifier(className, methodName)]
    }

    fun addInvokableFunction(className: String, methodName: String, resource: Resource) {
        invokableFunctions[FunctionIdentifier(className, methodName)] = resource
    }

}
