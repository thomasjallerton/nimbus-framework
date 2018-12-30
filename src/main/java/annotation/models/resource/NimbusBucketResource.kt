package annotation.models.resource

import org.json.JSONObject

class NimbusBucketResource: Resource {
    override fun getArn(suffix: String): JSONObject {
        return JSONObject()
    }

    override fun getName(): String {
        return "NimbusDeploymentBucket"
    }

    override fun toCloudFormation(): JSONObject {
        val bucketResource = JSONObject()
        bucketResource.put("Type", "AWS::S3::Bucket")
        return bucketResource
    }
}