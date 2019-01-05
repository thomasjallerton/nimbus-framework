package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonArray
import com.google.gson.JsonObject

abstract class Resource(protected val nimbusState: NimbusState) {
    abstract fun toCloudFormation(): JsonObject
    abstract fun getName(): String

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
}