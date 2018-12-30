package annotation.models.outputs

import org.json.JSONObject

interface Output {
    fun getName(): String
    fun toCloudFormation(): JSONObject
}