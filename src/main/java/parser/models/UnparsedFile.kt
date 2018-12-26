package parser.models

class UnparsedFile {

    private val lines : MutableList<String> = mutableListOf()

    fun add(line: String) {
        lines.add(line)
    }

    fun getLines(): List<String> {
        return lines
    }

    fun getLine(lineNumber: Int): String {
        return lines[lineNumber]
    }

    fun numberOfLines(): Int {
        return lines.size
    }
}