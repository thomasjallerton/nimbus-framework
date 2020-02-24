package com.nimbusframework.nimbusaws.arm.resources

import com.google.gson.JsonArray

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

    fun isEmpty(): Boolean {
        return resourceMap.isEmpty()
    }

    fun size(): Int {
        return resourceMap.size
    }

    fun toJson(): JsonArray {
        val resources = JsonArray()

        for (resource in resourceMap.values) {
            resources.add(resource.toJson())
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