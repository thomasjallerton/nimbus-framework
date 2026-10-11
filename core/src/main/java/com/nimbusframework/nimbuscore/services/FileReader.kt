package com.nimbusframework.nimbuscore.services

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.annotation.processing.Messager
import kotlin.io.path.Path
import kotlin.io.path.absolute

class FileReader {
    fun getFileText(path: String): String {

        try {
            val parsedPath = Path(path)
            val encoded = Files.readAllBytes(parsedPath)
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
