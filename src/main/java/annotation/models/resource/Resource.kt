package annotation.models.resource

import org.json.JSONObject

interface Resource {
    fun toCloudFormation(): JSONObject
}