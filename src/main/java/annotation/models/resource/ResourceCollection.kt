package annotation.models.resource

import org.json.JSONObject

class ResourceCollection {

    private val resourceMap: MutableMap<String, Resource> = mutableMapOf()

    fun addResource(resource: Resource) {
        resourceMap[resource.getName()] = resource
    }

    fun isEmpty(): Boolean {
        return resourceMap.isEmpty()
    }

    fun toJson(): JSONObject {
        val resources = JSONObject()
        for (resource in resourceMap.values) {
            resources.put(resource.getName(), resource.toCloudFormation())
        }

        return resources
    }

    fun contains(resource: Resource): Boolean {
        return resourceMap.containsKey(resource.getName())
    }

}