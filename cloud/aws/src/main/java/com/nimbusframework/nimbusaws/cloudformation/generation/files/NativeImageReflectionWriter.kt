package com.nimbusframework.nimbusaws.cloudformation.generation.files

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class NativeImageReflectionWriter(
    private val fileWriter: FileWriter
) {

    fun writeReflectionConfig(canonicalNames: Set<String>) {
        val file = JsonArray()
        for (name in canonicalNames) {
            val obj = JsonObject()
            obj.addProperty("name", name)
            obj.addProperty("allDeclaredConstructors", true)
            obj.addProperty("allDeclaredClasses", true)
            obj.addProperty("allPublicMethods", true)
            obj.addProperty("allDeclaredMethods", true)
            obj.addProperty("allPublicFields", true)
            obj.addProperty("allDeclaredFields", true)
            obj.addProperty("allPublicClasses", true)
            obj.addProperty("allPublicConstructors", true)
            file.add(obj)
        }

        val content = GsonBuilder().setPrettyPrinting().create().toJson(file)
        fileWriter.saveJsonFile("reflect-config", content)
    }

}
