package annotation.models.resource

import com.google.gson.JsonObject

class ResourceCollection {

    private val resourceMap: MutableMap<String, Resource> = mutableMapOf()

    fun addResource(resource: Resource) {
        if (!resourceMap.containsKey(resource.getName())) {
            resourceMap[resource.getName()] = resource
        }
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

}