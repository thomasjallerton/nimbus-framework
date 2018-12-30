package annotation.models.outputs

import org.json.JSONObject

class OutputCollection {

    private val outputMap: MutableMap<String, Output> = mutableMapOf()

    fun addOutput(output: Output) {
        outputMap[output.getName()] = output
    }

    fun isEmpty(): Boolean {
        return outputMap.isEmpty()
    }

    fun toJson(): JSONObject {
        val outputs = JSONObject()
        for (output in outputMap.values) {
            outputs.put(output.getName(), output.toCloudFormation())
        }

        return outputs
    }

    fun contains(output: Output): Boolean {
        return outputMap.containsKey(output.getName())
    }

}