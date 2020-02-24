package com.nimbusframework.nimbusaws.arm.resources

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class Resource(protected val nimbusState: NimbusState, val stage: String) {

    protected abstract fun configureJson(jsonObj: JsonObject)

    abstract fun getName(): String
    abstract fun getType(): String
    abstract fun getApiVersion(): String
    abstract fun getKind(): String

    private val properties: MutableMap<String, String> = mutableMapOf()
    private val jsonProperties: MutableMap<String, JsonObject> = mutableMapOf()
    protected val dependsOn: JsonArray = JsonArray()

    protected fun getProperties(): JsonObject {
        val propertiesJson = JsonObject()
        properties.forEach {(name, value) -> propertiesJson.addProperty(name, value)}
        jsonProperties.forEach {(name, value) -> propertiesJson.add(name, value)}
        return propertiesJson
    }

    fun toJson(): JsonObject {
        val root = JsonObject()
        root.addProperty("type", getType())
        root.addProperty("apiVersion", getApiVersion())
        root.addProperty("name", getName())
        root.addProperty("location", "[resourceGroup().location]")

        if (getKind().isNotBlank()) root.addProperty("kind", getKind())

        configureJson(root)

        if (dependsOn.size() != 0) root.add("dependsOn", dependsOn)

        return root
    }

    fun getRegion(): JsonObject {
        val region = JsonObject()
        region.addProperty("Ref", "AWS::Region")
        return region
    }

    fun addExtraProperty(name: String, value: String) {
        properties[name] = value
    }

    fun addExtraProperty(name: String, value: JsonObject) {
        jsonProperties[name] = value
    }

    fun addDependsOn(resource: Resource) {
        dependsOn.add("[resourceId('${resource.getType()}', '${resource.getName()}')]")
    }

    fun getShortProjectName(): String {
        return nimbusState.projectName.take(5)
    }

    companion object {

        fun getProperty(name: String, value: String): JsonObject {
            val jsonObject = JsonObject()
            jsonObject.addProperty("name", name)
            jsonObject.addProperty("value", value)
            return jsonObject
        }
    }
}