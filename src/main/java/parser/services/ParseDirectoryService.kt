package parser.services

import parser.models.unparsed.UnparsedFile

class ParseDirectoryService {

    fun parseFile(unparsedFile: UnparsedFile) {
        val lines = unparsedFile.getLines()

        var currentToken = ""
        for (line in lines) {

        }
    }
}
