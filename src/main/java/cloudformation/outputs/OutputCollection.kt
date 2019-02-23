package cloudformation.outputs

import com.google.gson.JsonObject

class OutputCollection {

    private val outputMap: MutableMap<String, Output> = mutableMapOf()

    fun addOutput(output: Output) {
        if (!outputMap.containsKey(output.getName())) {
            outputMap[output.getName()] = output
        }
    }

    fun isEmpty(): Boolean {
        return outputMap.isEmpty()
    }

    fun toJson(): JsonObject {
        val outputs = JsonObject()
        for (output in outputMap.values) {
            outputs.add(output.getName(), output.toCloudFormation())
        }

        return outputs
    }

    fun contains(output: Output): Boolean {
        return outputMap.containsKey(output.getName())
    }

}