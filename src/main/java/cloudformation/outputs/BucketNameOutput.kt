package cloudformation.outputs

import persisted.NimbusState
import cloudformation.resource.NimbusBucketResource
import com.google.gson.JsonObject
import configuration.DEPLOYMENT_BUCKET_NAME

class BucketNameOutput(
        private val bucketResource: NimbusBucketResource,
        nimbusState: NimbusState
): Output(nimbusState, bucketResource.stage) {
    override fun getName(): String {
        return DEPLOYMENT_BUCKET_NAME
    }

    override fun toCloudFormation(): JsonObject {
        val bucketName = JsonObject()
        val value = JsonObject()

        value.addProperty("Ref", bucketResource.getName())
        bucketName.add("Value", value)

        val export = JsonObject()
        export.addProperty("Name", "${nimbusState.projectName}-$stage-$DEPLOYMENT_BUCKET_NAME")

        bucketName.add("Export", export)

        return bucketName
    }
}
