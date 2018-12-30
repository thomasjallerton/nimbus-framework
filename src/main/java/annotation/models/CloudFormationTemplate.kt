package annotation.models

import annotation.models.resource.ResourceCollection
import org.json.JSONObject

class CloudFormationTemplate(private val resources: ResourceCollection) {

    fun valid(): Boolean {
        return !resources.isEmpty()
    }

    override fun toString(): String {
        val template = JSONObject()

        template.put("AWSTemplateFormatVersion", "2010-09-09")
        template.put("Description", "The AWS CloudFormation template for this Nimbus application")
        template.put("Resources", resources.toJson())

        return template.toString(2)
    }

//    "AWSTemplateFormatVersion": "2010-09-09",
//    "Description": "The AWS CloudFormation template for this Serverless application",
}