package cloudformation.resource.file

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class NotificationConfiguration {

    private val lambdaConfigurations: MutableList<LambdaConfiguration> = mutableListOf()

    fun toJson(): JsonObject {
        val notificationConfiguration = JsonObject()

        val lambdaConfigurationsArray = JsonArray()
        for (lambdaConfiguration in lambdaConfigurations) {
            lambdaConfigurationsArray.add(lambdaConfiguration.toJson())
        }

        notificationConfiguration.add("LambdaConfigurations", lambdaConfigurationsArray)

        return notificationConfiguration
    }

    fun addLambdaConfiguration(lambdaConfiguration: LambdaConfiguration) {
        lambdaConfigurations.add(lambdaConfiguration)
    }
}