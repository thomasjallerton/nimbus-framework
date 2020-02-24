package com.nimbusframework.nimbusaws.arm.resources.function

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.arm.resources.Resource
import com.nimbusframework.nimbuscore.persisted.NimbusState

class ApplicationInsights(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {

    override fun configureJson(jsonObj: JsonObject) {
        val properties = getProperties();
        properties.addProperty("Application_Type", "web")
        properties.addProperty("ApplicationId", getName())

        jsonObj.add("properties", properties)
    }

    override fun getName(): String {
        return "${nimbusState.projectName}appinsights$stage".toLowerCase()
    }

    override fun getType(): String {
        return "Microsoft.Insights/components"
    }

    override fun getApiVersion(): String {
        return "2015-05-01"
    }

    override fun getKind(): String {
        return "web"
    }

}