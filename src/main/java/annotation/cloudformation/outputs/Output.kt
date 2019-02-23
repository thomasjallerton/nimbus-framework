package annotation.cloudformation.outputs

import annotation.cloudformation.persisted.NimbusState
import com.google.gson.JsonObject

abstract class Output(protected val nimbusState: NimbusState) {
    abstract fun getName(): String
    abstract fun toCloudFormation(): JsonObject
}