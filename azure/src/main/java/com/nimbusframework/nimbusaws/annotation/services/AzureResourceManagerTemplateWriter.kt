package com.nimbusframework.nimbusaws.annotation.services

import java.io.IOException
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

class AzureResourceManagerTemplateWriter(private val filer: Filer) {
//    fun saveTemplate(name: String, template: CloudFormationTemplate) {
//        if (template.valid()) {
//            saveJsonFile(name, template.toJson())
//        }
//    }

    fun saveJsonFile(name: String, fileContent: String) {
        try {
            val file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "nimbus", "$name.json")
            val writer = file.openWriter()
            writer.write(fileContent)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}