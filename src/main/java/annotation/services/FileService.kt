package annotation.services

import annotation.cloudformation.CloudFormationTemplate
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class FileService {
    fun saveTemplate(name: String, template: CloudFormationTemplate) {
        if (template.valid()) {
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
        }

        return ""
    }

    fun getResourceFileText(path: String): String {
        val result = StringBuilder("")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(path)!!.file)

        try {
            Scanner(file).use { scanner ->

                while (scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    result.append(line).append("\n")
                }

                scanner.close()

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


        return result.toString()
    }
}