package com.nimbusframework.nimbusaws.arm.resources.filestorage

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.arm.resources.Resource
import com.nimbusframework.nimbuscore.persisted.NimbusState

class StorageAccount(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {

    override fun configureJson(jsonObj: JsonObject) {
        val sku = JsonObject()
        sku.addProperty("name", "Standard_LRS")
        jsonObj.add("sku", sku)
    }

    override fun getName(): String {
        return "${getShortProjectName()}store$stage".toLowerCase()
    }

    override fun getType(): String {
        return "Microsoft.Storage/storageAccounts"
    }

    override fun getApiVersion(): String {
        return "2019-04-01"
    }

    override fun getKind(): String {
        return "StorageV2"
    }

    fun getStorageAccountId(): String {
        return "[concat(resourceGroup().id,'/providers/','Microsoft.Storage/storageAccounts/', '${getName()}')]"
    }

}