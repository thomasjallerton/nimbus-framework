package cloudformation.resource.file

import annotation.wrappers.WebsiteConfiguration
import cloudformation.resource.Resource
import cloudformation.resource.function.FunctionTrigger
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import persisted.NimbusState

class FileBucket(
        nimbusState: NimbusState,
        private val name: String,
        stage: String
): Resource(nimbusState, stage), FunctionTrigger {

    private var hasNotificationConfiguration: Boolean = false
    private var hasWebsiteConfiguration: Boolean = false

    override fun getTriggerType(): String {
        return "s3."
    }

    override fun getArn(suffix: String): JsonObject {
        val values = JsonArray()
        values.add("arn:aws:s3:::")
        values.add(bucketName + suffix)
        return joinJson("", values)
    }

    override fun getTriggerArn(): JsonObject {
        val values = JsonArray()
        values.add("arn:aws:s3:::")
        values.add(bucketName)
        return joinJson("", values)
    }

    private val bucketName: String = "$name$stage".toLowerCase()
    private val notificationConfiguration: NotificationConfiguration = NotificationConfiguration()
    private var websiteConfiguration: WebsiteConfiguration = WebsiteConfiguration()

    override fun toCloudFormation(): JsonObject {
        val bucketResource = JsonObject()
        bucketResource.addProperty("Type","AWS::S3::Bucket")
        val properties = JsonObject()

        if (hasNotificationConfiguration) {
            properties.add("NotificationConfiguration", notificationConfiguration.toJson())
        }

        if (hasWebsiteConfiguration && websiteConfiguration.enabled) {
            properties.addProperty("AccessControl", "PublicRead")
            properties.add("WebsiteConfiguration", websiteConfiguration.toJson())
        }

        properties.addProperty("BucketName", bucketName)

        bucketResource.add("Properties", properties)

        bucketResource.add("DependsOn", dependsOn)
        return bucketResource
    }

    override fun getName(): String {
        return "${nimbusState.projectName}${name}FileBucket"
    }

    fun addLambdaConfiguration(lambdaConfiguration: LambdaConfiguration) {
        hasNotificationConfiguration = true
        notificationConfiguration.addLambdaConfiguration(lambdaConfiguration)
    }

    fun setWebsiteConfiguration(websiteConfiguration: WebsiteConfiguration) {
        hasWebsiteConfiguration = true
        this.websiteConfiguration = websiteConfiguration
    }
}