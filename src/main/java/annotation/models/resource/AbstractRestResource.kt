package annotation.models.resource

import annotation.models.persisted.NimbusState
import com.google.gson.JsonObject

abstract class AbstractRestResource (
        nimbusState: NimbusState
): Resource(nimbusState) {

    abstract fun getId(): JsonObject

    abstract fun getRootId(): JsonObject

    abstract fun getPath(): String
}