package assembly

import javassist.bytecode.ClassFile
import javassist.bytecode.ConstPool
import org.apache.commons.codec.digest.DigestUtils
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.util.*

class FunctionHasher(private val targetDirectory: String) {

    private val alreadyProcessed: MutableMap<String, String?> = mutableMapOf()

    fun determineFunctionHash(handlerClassPath: String): String {
        if (handlerClassPath.isBlank()) {
            return ""
        }
        return recursiveDetermineFunctionHash(handlerClassPath) ?: ""
    }

    private fun recursiveDetermineFunctionHash(filePath: String): String? {
        if (alreadyProcessed.containsKey(filePath)) return alreadyProcessed[filePath]
        alreadyProcessed[filePath] = null
        val handlerFilePath = determineLocalPath(filePath)
        val handlerFile = File(handlerFilePath)
        val hash =  if (handlerFile.exists()) {
            val fileInputStream = handlerFile.inputStream()
            val fileHash = DigestUtils.sha256Hex(fileInputStream)
            fileInputStream.close()

            val dependencies = getDependenciesOfFile(handlerFile.inputStream())
            val concatenatedHashes = dependencies.mapNotNull {
                val path = it.replace('.', File.separatorChar)
                recursiveDetermineFunctionHash(path)
            }.sorted().fold(fileHash) {acc, item -> acc + item}

            DigestUtils.md5Hex(concatenatedHashes)
        } else {
            null
        }
        alreadyProcessed[filePath] = hash
        return hash
    }

    private fun determineLocalPath(classPath: String): String {
        return targetDirectory + File.separator + classPath + ".class"
    }

    private fun getDependenciesOfFile(inputStream: InputStream): MutableSet<String> {
        val cf = ClassFile(DataInputStream(inputStream))
        val constPool = cf.constPool
        val set = HashSet<String>()
        for (ix in 1 until constPool.size) {
            val constTag = constPool.getTag(ix)
            if (constTag == ConstPool.CONST_Class) {
                set.add(constPool.getClassInfo(ix))
            } else {
                val descriptorIndex = when (constTag) {
                    ConstPool.CONST_NameAndType -> constPool.getNameAndTypeDescriptor(ix)
                    ConstPool.CONST_MethodType -> constPool.getMethodTypeInfo(ix)
                    else -> -1
                }

                if (descriptorIndex != -1) {
                    val desc = constPool.getUtf8Info(descriptorIndex)
                    var p = 0
                    while (p < desc.length) {
                        if (desc[p] == 'L') {
                            val semiColonIndex = desc.indexOf(';', p)
                            val toAdd = desc.substring(p + 1, semiColonIndex).replace('/', '.')
                            set.add(toAdd)
                            p = semiColonIndex
                        }
                        p++
                    }

                }

            }
        }
        inputStream.close()
        return set
    }
}
