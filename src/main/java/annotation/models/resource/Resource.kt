package annotation.models.resource

import annotation.models.persisted.NimbusState
import org.json.JSONArray
import org.json.JSONObject

abstract class Resource(protected val nimbusState: NimbusState) {
    abstract fun toCloudFormation(): JSONObject
    abstract fun getName(): String

    open fun getArn(suffix: String = ""): JSONObject {
        val arn = JSONObject()
        val roleFunc = JSONArray()
        roleFunc.put(getName())
        roleFunc.put("Arn")
        arn.put("Fn::GetAtt", roleFunc)
        return arn
    }

    open fun getRef(): JSONObject {
        val ref =  JSONObject()
        ref.put("Ref", getName())
        return ref
    }
}