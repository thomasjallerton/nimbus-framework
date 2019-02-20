package annotation.services

import annotation.models.CloudFormationTemplate
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class FileService {
    fun saveTemplate(name: String, template: CloudFormationTemplate) {
        println("SAVING TEMPLATE")
        if (template.valid()) {
            println("TEMPLATE IS VALID")
            saveJsonFile(name, template.getJsonTemplate())
        }
    }

    fun saveJsonFile(name: String, file: String) {
        try {
            val path = Paths.get(".nimbus/$name.json")
            path.toFile().parentFile.mkdirs()
            val strToBytes = file.toByteArray()
            Files.write(path, strToBytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getFileText(path: String): String {

        try {
            val encoded = Files.readAllBytes(Paths.get(path))
            return String(encoded)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }
}