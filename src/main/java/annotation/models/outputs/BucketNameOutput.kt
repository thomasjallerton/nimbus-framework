package annotation.models.outputs

import org.json.JSONObject

class BucketNameOutput: Output {
    override fun getName(): String {
        return "NimbusDeploymentBucketName"
    }

    override fun toCloudFormation(): JSONObject {
        val bucketName = JSONObject()
        val value = JSONObject()

        value.put("Ref", "NimbusDeploymentBucket")
        bucketName.put("Value", value)

        val export = JSONObject()
        export.put("Name", "NimbusProjectBucketName")

        bucketName.put("Export", export)

        return bucketName
    }
}
