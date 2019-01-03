package annotation.models.outputs

import annotation.models.persisted.NimbusState
import com.google.gson.JsonObject

abstract class Output(protected val nimbusState: NimbusState) {
    abstract fun getName(): String
    abstract fun toCloudFormation(): JsonObject
}