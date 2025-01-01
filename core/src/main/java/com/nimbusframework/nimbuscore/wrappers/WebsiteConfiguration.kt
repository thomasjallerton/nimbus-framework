package com.nimbusframework.nimbuscore.wrappers

import com.google.gson.JsonObject

data class WebsiteConfiguration(
        val enabled: Boolean = false,
        val publicAccess: Boolean = true,
        val index: String = "index.html",
        val error: String = "error.html"
) {
    fun toJson(): JsonObject {
        val websiteConfiguration = JsonObject()

        websiteConfiguration.addProperty("IndexDocument", index)
        websiteConfiguration.addProperty("ErrorDocument", error)

        return websiteConfiguration
    }
}
