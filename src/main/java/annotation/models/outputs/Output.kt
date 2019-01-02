package annotation.models.outputs

import annotation.models.persisted.NimbusState
import org.json.JSONObject

abstract class Output(protected val nimbusState: NimbusState) {
    abstract fun getName(): String
    abstract fun toCloudFormation(): JSONObject
}