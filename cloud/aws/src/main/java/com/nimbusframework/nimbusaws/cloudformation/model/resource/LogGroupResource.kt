package com.nimbusframework.nimbusaws.cloudformation.model.resource

import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.tools.Diagnostic

class LogGroupResource(
    nimbusState: NimbusState,
    stage: String
): Resource(nimbusState, stage) {

    override fun getName(): String {
        return "LogGroup${nimbusState.projectName}${stage}"
    }

    override fun toCloudFormation(): JsonObject {
        val logGroupResource = JsonObject()

        logGroupResource.addProperty("Type", "AWS::Logs::LogGroup")

        val properties = getProperties()
        properties.addProperty("LogGroupName", logGroupName())
        if (nimbusState.logGroupRetentionInDays != null) {
            properties.addProperty("RetentionInDays", nimbusState.logGroupRetentionInDays)
        }

        logGroupResource.add("Properties", properties)

        return logGroupResource
    }

    fun loggingConfig(): JsonObject {
        val loggingConfig = JsonObject()
        loggingConfig.addProperty("LogGroup", logGroupName())
        return loggingConfig
    }

    private fun logGroupName(): String {
        return "${nimbusState.projectName}-${stage}-logs"
    }

}
