package com.nimbusframework.nimbuscore.cloudformation.resource

import com.google.gson.JsonObject

class ResourceCollection {

    private val resourceMap: MutableMap<String, Resource> = mutableMapOf()
    private val invokableFunctions: MutableList<Resource> = mutableListOf()

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

    fun getInvokableFunctions(): List<Resource> {
        return invokableFunctions
    }

    fun addInvokableFunction(resource: Resource) {
        invokableFunctions.add(resource)
    }



}