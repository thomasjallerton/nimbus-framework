package annotation.models

import annotation.models.resource.Resource
import org.json.JSONObject

class CloudFormationTemplate(private val resources: JSONObject) {

    override fun toString(): String {
        val template = JSONObject()

        template.put("AWSTemplateFormatVersion", "2010-09-09")
        template.put("Description", "The AWS CloudFormation template for this Nimbus application")
        template.put("Resources", resources)

        return template.toString()
    }

//    "AWSTemplateFormatVersion": "2010-09-09",
//    "Description": "The AWS CloudFormation template for this Serverless application",
}