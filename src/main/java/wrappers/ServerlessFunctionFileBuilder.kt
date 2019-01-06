package wrappers

import annotation.models.processing.MethodInformation
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror

abstract class ServerlessFunctionFileBuilder(
        private val processingEnv: ProcessingEnvironment,
        private val methodInformation: MethodInformation
) {

    private var tabLevel: Int = 0

    protected var out: PrintWriter? = null

    protected val messager: Messager = processingEnv.messager

    protected abstract fun getGeneratedClassName(): String

    fun getHandler(): String {
        return if (customFunction()) {
            if (methodInformation.qualifiedName == "") {
                "${methodInformation.className}::${methodInformation.methodName}"
            } else {
                "${methodInformation.qualifiedName}.${methodInformation.className}::${methodInformation.methodName}"
            }
        } else {
            "${getGeneratedClassName()}::nimbusHandle"
        }
    }

    protected fun write(toWrite: String = "") {
        if (toWrite.startsWith("}")) tabLevel--

        var tabs = ""
        for (i in 1..tabLevel) {
            tabs += "\t"
        }
        out?.println("$tabs$toWrite")

        if (toWrite.endsWith("{")) tabLevel++
    }

    protected fun customFunction(): Boolean {
        val params = methodInformation.parameters
        if (params.size == 3) {
            return (params[0].toString().contains("InputStream") &&
                    params[1].toString().contains("OutputStream") &&
                    params[2].toString().contains("Context"))
        }
        return false
    }

    protected fun findInputTypeAndIndex(): InputParam {
        var inputParamIndex = 0
        for (param in methodInformation.parameters) {
            if (param.toString() != "wrappers.http.models.HttpEvent") {
                return InputParam(param, inputParamIndex)
            } else {
                inputParamIndex++
            }
        }
        return InputParam(null, 0)
    }

    protected data class InputParam(val type: TypeMirror?, val index: Int)

    protected fun findPackageName(qualifiedName: String): String {
        val lastDot = qualifiedName.lastIndexOf('.')
        return if (lastDot > 0) {
            qualifiedName.substring(0, lastDot)
        } else {
            ""
        }
    }
}