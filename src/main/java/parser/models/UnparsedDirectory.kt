package parser.models

class UnparsedDirectory {

    private val unparsedFiles: MutableMap<String, UnparsedFile> = mutableMapOf()

    fun addFile(src: String, file: UnparsedFile) {
        unparsedFiles[src] = file
    }

    fun addSubDirectory(unparsedDirectory: UnparsedDirectory) {
        unparsedFiles.putAll(unparsedDirectory.getMap())
    }

    fun getAllFiles(): List<UnparsedFile> {
        return unparsedFiles.values.toList()
    }

    fun getMap(): Map<String, UnparsedFile> {
        return unparsedFiles
    }

    fun getFile(src: String): UnparsedFile {
        return if (unparsedFiles.contains(src)) {
            unparsedFiles[src]!!
        } else {
            UnparsedFile()
        }
    }
}