package annotation.models.resource

import org.json.JSONObject

interface Resource {
    fun toCloudFormation(): JSONObject
    fun getName(): String
    fun getArn(suffix: String = ""): JSONObject
}