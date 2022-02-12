package com.nimbusframework.nimbusaws.cloudformation.resource.file

import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nimbusframework.nimbuscore.persisted.NimbusState

class FileStorageBucketPolicy(
        nimbusStage: NimbusState,
        private val bucketResource: FileBucket,
        stage: String
): Resource(nimbusStage, stage) {

    override fun toCloudFormation(): JsonObject {
        val bucketPolicy = JsonObject()

        bucketPolicy.addProperty("Type", "AWS::S3::BucketPolicy")

        val properties = JsonObject()
        properties.add("Bucket", bucketResource.getRef())

        val policyDocument = JsonObject()

        policyDocument.addProperty("Id", bucketResource.getName() + "Policy")
        policyDocument.addProperty("Version", "2012-10-17")

        val statements = JsonArray()
        val statement = JsonObject()

        statement.addProperty("Sid", "PublicReadFor" + bucketResource.getName())
        statement.addProperty("Effect", "Allow")
        statement.addProperty("Principal", "*")
        statement.addProperty("Action", "s3:GetObject")
        statement.add("Resource", bucketResource.getArn("/*"))

        statements.add(statement)
        policyDocument.add("Statement", statements)

        properties.add("PolicyDocument", policyDocument)

        bucketPolicy.add("Properties", properties)

        return bucketPolicy
    }

    override fun getName(): String {
        return "Policy" + bucketResource.getName()
    }
}