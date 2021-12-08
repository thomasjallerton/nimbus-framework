package com.nimbusframework.nimbusaws.cloudformation.resource.file

import com.nimbusframework.nimbuscore.wrappers.WebsiteConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionTrigger
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FileBucket(
        nimbusState: NimbusState,
        val annotationBucketName: String,
        private val allowedCorsOrigins: Array<String>,
        stage: String
): Resource(nimbusState, stage), FunctionTrigger {

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

    val bucketName: String = "$annotationBucketName$stage".toLowerCase()
    private val s3NotificationConfiguration: S3NotificationConfiguration = S3NotificationConfiguration()
    private var websiteConfiguration: WebsiteConfiguration = WebsiteConfiguration()

    override fun toCloudFormation(): JsonObject {
        val bucketResource = JsonObject()
        bucketResource.addProperty("Type","AWS::S3::Bucket")
        val properties = JsonObject()

        if (hasNotificationConfiguration) {
            properties.add("NotificationConfiguration", s3NotificationConfiguration.toJson())
        }

        if (hasWebsiteConfiguration && websiteConfiguration.enabled) {
            properties.addProperty("AccessControl", "PublicRead")
            properties.add("WebsiteConfiguration", websiteConfiguration.toJson())
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