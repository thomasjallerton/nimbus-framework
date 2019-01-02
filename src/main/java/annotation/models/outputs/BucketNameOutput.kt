package annotation.models.outputs

import annotation.models.persisted.NimbusState
import annotation.models.resource.NimbusBucketResource
import configuration.DEPLOYMENT_BUCKET_NAME
import org.json.JSONObject

class BucketNameOutput(
        private val bucketResource: NimbusBucketResource,
        nimbusState: NimbusState
): Output(nimbusState) {
    override fun getName(): String {
        return DEPLOYMENT_BUCKET_NAME
    }

    override fun toCloudFormation(): JSONObject {
        val bucketName = JSONObject()
        val value = JSONObject()

        value.put("Ref", bucketResource.getName())
        bucketName.put("Value", value)

        val export = JSONObject()
        export.put("Name", "${nimbusState.projectName}-$DEPLOYMENT_BUCKET_NAME")

        bucketName.put("Export", export)

        return bucketName
    }
}
