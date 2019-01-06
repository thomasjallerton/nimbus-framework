package annotation.models

import annotation.models.outputs.OutputCollection
import annotation.models.resource.ResourceCollection
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject


class CloudFormationTemplate(
        private val resources: ResourceCollection,
        private val outputs: OutputCollection) {

    fun valid(): Boolean {
        return !resources.isEmpty()
    }

    fun getJsonTemplate(): String {
        val template = JsonObject()

        template.addProperty("AWSTemplateFormatVersion", "2010-09-09")
        template.addProperty("Description", "The AWS CloudFormation template for this Nimbus application")
        template.add("Resources", resources.toJson())

        if (!outputs.isEmpty()) {
            template.add("Outputs", outputs.toJson())
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(template)
    }

}