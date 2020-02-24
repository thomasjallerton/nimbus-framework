package com.nimbusframework.nimbusaws.arm.resources.function

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.arm.resources.Resource
import com.nimbusframework.nimbuscore.persisted.NimbusState

class ServerFarm(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {

    override fun configureJson(jsonObj: JsonObject) {
        val properties = getProperties()
        properties.addProperty("name", getName())
        properties.addProperty("computeMode", "Dynamic")

        val sku = JsonObject()
        sku.addProperty("name", "Y1")
        sku.addProperty("tier", "Dyanmic")
        sku.addProperty("size", "Y1")
        sku.addProperty("family", "Y")
        sku.addProperty("capacity", 0)

        jsonObj.add("properties", properties)
        jsonObj.add("sku", sku)
    }

    override fun getName(): String {
        return "${nimbusState.projectName.take(5)}serverfarm$stage".toLowerCase()
    }

    override fun getType(): String {
        return "Microsoft.Web/serverfarms"
    }

    override fun getApiVersion(): String {
        return "2016-09-01"
    }

    override fun getKind(): String {
        return ""
    }
}