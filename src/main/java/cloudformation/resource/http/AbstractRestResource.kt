package cloudformation.resource.http

import persisted.NimbusState
import cloudformation.resource.Resource
import com.google.gson.JsonObject

abstract class AbstractRestResource (
        nimbusState: NimbusState
): Resource(nimbusState) {

    abstract fun getId(): JsonObject

    abstract fun getRootId(): JsonObject

    abstract fun getPath(): String
}