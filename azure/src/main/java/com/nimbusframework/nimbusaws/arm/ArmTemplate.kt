package com.nimbusframework.nimbusaws.arm

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.arm.resources.Resource
import com.nimbusframework.nimbusaws.arm.resources.ResourceCollection
import com.nimbusframework.nimbusaws.arm.resources.filestorage.StorageAccount
import com.nimbusframework.nimbuscore.persisted.NimbusState

data class ArmTemplate(
        private val nimbusState: NimbusState,
        private val stage: String,
        val resources: ResourceCollection = ResourceCollection(),
        val variables: MutableMap<String, String> = mutableMapOf()
) {

    init {
        addResource(StorageAccount(nimbusState, stage))
    }

    fun addResource(resource: Resource) {
        resources.addResource(resource)
    }

    fun valid(): Boolean {
        return true;
    }

    fun toJson(): String {
        val root = JsonObject()

        root.addProperty("\$schema", "https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json")
        root.addProperty("contentVersion", "1.0.0.0")
        root.add("resources", resources.toJson())

        val jsonVariables = JsonObject()
        for (variable in variables) {
            jsonVariables.addProperty(variable.key, variable.value)
        }

        root.add("variables", jsonVariables)

        val gson = GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
        return gson.toJson(root)
//        return "{\n" +
//                "    \"\$schema\": \"https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\n" +
//                "    \"contentVersion\": \"1.0.0.0\",\n" +
//                "    \"parameters\": {\n" +
//                "        \"siteName\": {\n" +
//                "            \"type\": \"string\",\n" +
//                "            \"defaultValue\": \"[concat('FuncApp-', uniqueString(resourceGroup().id))]\",\n" +
//                "            \"metadata\": {\n" +
//                "                \"description\": \"The name of you Web Site.\"\n" +
//                "            }\n" +
//                "        },\n" +
//                "        \"location\": {\n" +
//                "        \"type\": \"string\",\n" +
//                "        \"defaultValue\": \"[resourceGroup().location]\",\n" +
//                "        \"metadata\": {\n" +
//                "                \"description\": \"Location for all resources.\"\n" +
//                "            }\n" +
//                "        }\n" +
//                "    },\n" +
//                "     \"variables\": {\n" +
//                "        \"hostingPlanName\": \"[concat('hpn-', resourceGroup().name)]\"\n" +
//                "    },\n" +
//                "    \"resources\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Microsoft.Web/sites\",\n" +
//                "            \"apiVersion\": \"2018-02-01\",\n" +
//                "            \"name\": \"[parameters('siteName')]\",\n" +
//                "            \"kind\": \"functionapp,linux\",\n" +
//                "            \"location\": \"[parameters('location')]\",\n" +
//                "            \"dependsOn\": [\n" +
//                "                \"[resourceId('Microsoft.Web/serverfarms', variables('hostingPlanName'))]\"\n" +
//                "            ],\n" +
//                "            \"properties\": {\n" +
//                "                \"name\": \"[parameters('siteName')]\",\n" +
//                "                \"siteConfig\": {\n" +
//                "                     \"appSettings\": [\n" +
//                "                        {\n" +
//                "                            \"name\": \"FUNCTIONS_WORKER_RUNTIME\",\n" +
//                "                            \"value\": \"python\"\n" +
//                "                        },\n" +
//                "                        {\n" +
//                "                            \"name\": \"FUNCTIONS_EXTENSION_VERSION\",\n" +
//                "                            \"value\": \"~2\"\n" +
//                "                        }\n" +
//                "                    ]\n" +
//                "                },\n" +
//                "                \"serverFarmId\": \"[resourceId('Microsoft.Web/serverfarms', variables('hostingPlanName'))]\",\n" +
//                "                \"clientAffinityEnabled\": false\n" +
//                "            }\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"type\": \"Microsoft.Web/serverfarms\",\n" +
//                "            \"apiVersion\": \"2018-02-01\",\n" +
//                "            \"name\": \"[variables('hostingPlanName')]\",\n" +
//                "            \"location\": \"[parameters('location')]\",\n" +
//                "            \"kind\": \"linux\",\n" +
//                "            \"properties\":{\n" +
//                "                \"reserved\": false\n" +
//                "            },\n" +
//                "            \"sku\": {\n" +
//                "                \"Tier\": \"Dynamic\",\n" +
//                "                \"Name\": \"Y1\"\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}"
    }

}