package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonObject

class NimbusBucketResource(nimbusState: NimbusState): Resource(nimbusState) {

    override fun getName(): String {
        return "NimbusDeploymentBucket"
    }

    override fun toCloudFormation(): JsonObject {
        val bucketResource = JsonObject()
        bucketResource.addProperty("Type","AWS::S3::Bucket")
        return bucketResource
    }
}