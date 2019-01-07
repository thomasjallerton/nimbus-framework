package annotation.models.resource.http

import annotation.models.persisted.NimbusState
import annotation.models.resource.Resource
import com.google.gson.JsonObject

abstract class AbstractRestResource (
        nimbusState: NimbusState
): Resource(nimbusState) {

    abstract fun getId(): JsonObject

    abstract fun getRootId(): JsonObject

    abstract fun getPath(): String
}