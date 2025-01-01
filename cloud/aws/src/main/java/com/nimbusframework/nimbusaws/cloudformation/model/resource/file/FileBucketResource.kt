package com.nimbusframework.nimbusaws.cloudformation.model.resource.file

import com.nimbusframework.nimbuscore.wrappers.WebsiteConfiguration
import com.nimbusframework.nimbusaws.cloudformation.model.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.model.resource.DirectAccessResource
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FileBucketResource(
    nimbusState: NimbusState,
    val annotationBucketName: String,
    private val allowedCorsOrigins: Array<String>,
    stage: String
) : Resource(nimbusState, stage), FunctionTrigger, DirectAccessResource {

    private var hasNotificationConfiguration: Boolean = false
    private var hasWebsiteConfiguration: Boolean = false

    override fun getTriggerType(): String {
        return "s3."
    }

    override fun getTriggerName(): String {
        return "Bucket"
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

    val bucketName: String = "$annotationBucketName$stage".lowercase()
    private val s3NotificationConfiguration: S3NotificationConfiguration = S3NotificationConfiguration()
    private var websiteConfiguration: WebsiteConfiguration = WebsiteConfiguration()

    override fun toCloudFormation(): JsonObject {
        val bucketResource = JsonObject()
        bucketResource.addProperty("Type", "AWS::S3::Bucket")
        val properties = JsonObject()

        if (hasNotificationConfiguration) {
            properties.add("NotificationConfiguration", s3NotificationConfiguration.toJson())
        }

        if (hasWebsiteConfiguration && websiteConfiguration.enabled) {
            properties.add("WebsiteConfiguration", websiteConfiguration.toJson())

            val publicAccessBlockConfig = JsonObject()
            publicAccessBlockConfig.addProperty("BlockPublicAcls", "false")
            publicAccessBlockConfig.addProperty("BlockPublicPolicy", "false")
            publicAccessBlockConfig.addProperty("IgnorePublicAcls", "false")
            publicAccessBlockConfig.addProperty("RestrictPublicBuckets", "false")
            properties.add("PublicAccessBlockConfiguration", publicAccessBlockConfig)

            if (allowedCorsOrigins.isNotEmpty()) {
                val corsConfiguration = CorsConfiguration(
                    allowedCorsOrigins
                )
                properties.add("CorsConfiguration", corsConfiguration.toJson())
            }
        }

        properties.addProperty("BucketName", bucketName)

        bucketResource.add("Properties", properties)

        bucketResource.add("DependsOn", dependsOn)
        return bucketResource
    }

    override fun getName(): String {
        return "${nimbusState.projectName}${annotationBucketName}FileBucket"
    }

    fun addLambdaConfiguration(s3LambdaConfiguration: S3LambdaConfiguration) {
        hasNotificationConfiguration = true
        s3NotificationConfiguration.addLambdaConfiguration(s3LambdaConfiguration)
    }

    fun setWebsiteConfiguration(websiteConfiguration: WebsiteConfiguration) {
        hasWebsiteConfiguration = true
        this.websiteConfiguration = websiteConfiguration
    }
}
