package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class Resource(protected val nimbusState: NimbusState) {
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
        val arn = JsonObject()
        val roleFunc = JsonArray()
        roleFunc.add(getName())
        roleFunc.add("Arn")
        arn.add("Fn::GetAtt", roleFunc)
        return arn
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
}