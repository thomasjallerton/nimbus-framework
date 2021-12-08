package com.nimbusframework.nimbusaws.cloudformation.resource.file

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class S3NotificationConfiguration {

    private val s3LambdaConfigurations: MutableList<S3LambdaConfiguration> = mutableListOf()

    fun toJson(): JsonObject {
        val notificationConfiguration = JsonObject()

        val lambdaConfigurationsArray = JsonArray()
        for (lambdaConfiguration in s3LambdaConfigurations) {
            lambdaConfigurationsArray.add(lambdaConfiguration.toJson())
        }

        notificationConfiguration.add("LambdaConfigurations", lambdaConfigurationsArray)

        return notificationConfiguration
    }

    fun addLambdaConfiguration(s3LambdaConfiguration: S3LambdaConfiguration) {
        s3LambdaConfigurations.add(s3LambdaConfiguration)
    }
}