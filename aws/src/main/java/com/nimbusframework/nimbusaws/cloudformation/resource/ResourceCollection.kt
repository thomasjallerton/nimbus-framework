package com.nimbusframework.nimbusaws.cloudformation.resource

import com.google.gson.JsonObject

class ResourceCollection {

    private val resourceMap: MutableMap<String, Resource> = mutableMapOf()
    private val invokableFunctions: MutableMap<FunctionIdentifier,Resource> = mutableMapOf()

    fun addResource(resource: Resource) {
        if (!resourceMap.containsKey(resource.getName())) {
            resourceMap[resource.getName()] = resource
        }
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
            resources.add(resource.getName(), resource.toCloudFormation())
        }

        return resources
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

    private data class FunctionIdentifier(val className: String, val methodName: String)



}