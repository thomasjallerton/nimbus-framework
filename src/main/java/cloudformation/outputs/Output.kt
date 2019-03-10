package cloudformation.outputs

import persisted.NimbusState
import com.google.gson.JsonObject

abstract class Output(protected val nimbusState: NimbusState, protected val stage: String) {
    abstract fun getName(): String
    abstract fun toCloudFormation(): JsonObject
}