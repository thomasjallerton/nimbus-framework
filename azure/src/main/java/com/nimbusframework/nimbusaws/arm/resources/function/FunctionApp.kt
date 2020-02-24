package com.nimbusframework.nimbusaws.arm.resources.function

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.arm.resources.Resource
import com.nimbusframework.nimbusaws.arm.resources.filestorage.StorageAccount
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FunctionApp(
        private val storageAccount: StorageAccount,
        private val applicationInsights: ApplicationInsights,
        private val serverFarm: ServerFarm,
        private val variables: MutableMap<String, String>,
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage) {

    init {
        addDependsOn(storageAccount)
        addDependsOn(applicationInsights)
        addDependsOn(serverFarm)
    }

    override fun configureJson(jsonObj: JsonObject) {
        val properties = getProperties()
        val siteConfig = JsonObject()
        val appSettings = JsonArray()

        val storageAccountId = storageAccount.getName() + "Id"
        variables[storageAccountId] = storageAccount.getStorageAccountId()
        val endpoint = "[concat('DefaultEndpointsProtocol=https;AccountName=', '${storageAccount.getName()}', ';AccountKey=', listKeys(variables('$storageAccountId'),'2015-05-01-preview').key1)]"
        appSettings.add(getProperty("AzureWebJobsStorage", endpoint))
        appSettings.add(getProperty("WEBSITE_CONTENTAZUREFILECONNECTIONSTRING", endpoint))
        appSettings.add(getProperty("WEBSITE_CONTENTSHARE", getName().toLowerCase()))
        appSettings.add(getProperty("FUNCTIONS_EXTENSION_VERSION", "~2"))
        appSettings.add(getProperty("FUNCTIONS_WORKER_RUNTIME", "java"))
        appSettings.add(getProperty("APPINSIGHTS_INSTRUMENTATIONKEY", "[reference(resourceId('microsoft.insights/components/', '${applicationInsights.getName()}'), '2015-05-01').InstrumentationKey]"))

        siteConfig.add("appSettings", appSettings)
        properties.add("siteConfig", siteConfig)
        properties.addProperty("serverFarmId", "[resourceId('Microsoft.Web/serverfarms', '${serverFarm.getName()}')]")
        jsonObj.add("properties", properties)

    }

    override fun getName(): String {
        return "${nimbusState.projectName}functionapp$stage".toLowerCase()
    }

    override fun getType(): String {
        return "Microsoft.Web/sites"
    }

    override fun getApiVersion(): String {
        return "2015-08-01"
    }

    override fun getKind(): String {
        return "functionapp"
    }

}