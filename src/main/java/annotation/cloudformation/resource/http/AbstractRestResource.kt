package annotation.cloudformation.resource.http

import annotation.cloudformation.persisted.NimbusState
import annotation.cloudformation.resource.Resource
import com.google.gson.JsonObject

abstract class AbstractRestResource (
        nimbusState: NimbusState
): Resource(nimbusState) {

    abstract fun getId(): JsonObject

    abstract fun getRootId(): JsonObject

    abstract fun getPath(): String
}