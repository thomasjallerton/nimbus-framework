package com.nimbusframework.nimbusaws.cloudformation.model.resource

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class Resource(protected val nimbusState: NimbusState, val stage: String) {
    abstract fun toCloudFormation(): JsonObject
    abstract fun getName(): String

    private val properties: MutableMap<String, String> = mutableMapOf()
    private val jsonProperties: MutableMap<String, JsonObject> = mutableMapOf()
    protected val dependsOn: JsonArray = JsonArray()

    protected fun getProperties(): JsonObject {
        val propertiesJson = JsonObject()
        properties.forEach {(name, value) -> propertiesJson.addProperty(name, value)}
        jsonProperties.forEach {(name, value) -> propertiesJson.add(name, value)}
        return propertiesJson
    }

    open fun getArn(suffix: String = ""): JsonObject {
        return getAttr("Arn")
    }

    fun getAttr(property: String): JsonObject {
        val getAttr = JsonObject()
        val roleFunc = JsonArray()
        roleFunc.add(getName())
        roleFunc.add(property)
        getAttr.add("Fn::GetAtt", roleFunc)
        return getAttr
    }

    fun getRegion(): JsonObject {
        val region = JsonObject()
        region.addProperty("Ref", "AWS::Region")
        return region
    }

    open fun getRef(): JsonObject {
        val ref = JsonObject()
        ref.addProperty("Ref", getName())
        return ref
    }

    protected fun getRefProperty(value: String): JsonObject {
        val ref = JsonObject()
        ref.addProperty("Ref", value)
        return ref
    }

    fun addExtraProperty(name: String, value: String) {
        properties[name] = value
    }

    fun addExtraProperty(name: String, value: JsonObject) {
        jsonProperties[name] = value
    }

    fun getAttribute(attributeName: String): JsonObject {
        val attribute = JsonObject()
        val attributeArray = JsonArray()
        attributeArray.add(getName())
        attributeArray.add(attributeName)
        attribute.add("Fn::GetAtt", attributeArray)
        return attribute
    }

    fun addDependsOn(resource: Resource) {
        dependsOn.add(resource.getName())
    }

    fun joinJson(delimiter: String, values: JsonArray): JsonObject {
        val join = JsonObject()
        val params = JsonArray()
        params.add(delimiter)
        params.add(values)

        join.add("Fn::Join", params)

        return join
    }

    fun subFunc(strToSub: String): JsonObject {
        val sub = JsonObject()
        sub.addProperty("Fn::Sub", strToSub)
        return sub
    }

    open fun getAdditionalResources(): List<Resource> {
        return listOf()
    }

    protected fun toAlphanumeric(str: String): String {
        return str.replace(Regex("[^a-zA-Z0-9]"), "")
    }
}
