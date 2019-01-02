package annotation.models.resource

import annotation.models.persisted.NimbusState
import org.json.JSONObject

class NimbusBucketResource(nimbusState: NimbusState): Resource(nimbusState) {

    override fun getName(): String {
        return "NimbusDeploymentBucket"
    }

    override fun toCloudFormation(): JSONObject {
        val bucketResource = JSONObject()
        bucketResource.put("Type", "AWS::S3::Bucket")
        return bucketResource
    }
}