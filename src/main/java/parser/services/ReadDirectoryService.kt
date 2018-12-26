package parser.services

import parser.models.UnparsedDirectory
import parser.models.UnparsedFile
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Paths
import java.io.File



class ReadDirectoryService {

    private val workingDir = System.getProperty("user.dir")


    fun readDirectory(src: String): UnparsedDirectory {

        val correctedSrc = correctDirectoryPath(src)

        val unparsedDirectory = UnparsedDirectory()
        val folder = File(src)
        val listOfFiles = folder.listFiles()

        for (i in listOfFiles.indices) {
            if (listOfFiles[i].isFile) {
                val unparsedFile = readFile(correctedSrc + listOfFiles[i].name)
                unparsedDirectory.addFile(correctedSrc + listOfFiles[i].name, unparsedFile)
            } else if (listOfFiles[i].isDirectory) {
                val subDirectory = readDirectory(correctedSrc + listOfFiles[i].name)
                unparsedDirectory.addSubDirectory(subDirectory)
            }
        }

        return unparsedDirectory

    }

    fun readFile(src: String): UnparsedFile {
        val unparsedFile = UnparsedFile()

        File("$workingDir/$src").forEachLine { line ->
            unparsedFile.add(line)
        }

        return unparsedFile
    }

    private fun correctDirectoryPath(src: String): String {
        return if (src.endsWith("/")) src else "$src/"
    }
}