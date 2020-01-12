package com.nimbusframework.nimbusaws.wrappers

import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import java.io.PrintWriter
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

abstract class FileBuilder {

    protected var out: PrintWriter? = null

    private var tabLevel: Int = 0

    protected fun write(toWrite: String = "") {
        if (toWrite.startsWith("}")) tabLevel--

        var tabs = ""
        for (i in 1..tabLevel) {
            tabs += "\t"
        }
        out?.println("$tabs$toWrite")

        if (toWrite.endsWith("{")) tabLevel++
    }

    protected fun import(toImport: String) {
        write("import $toImport;")
    }

    protected fun findParamIndexes(methodInformation: MethodInformation, eventCanonicalName: String): FunctionParams {
        val functionParams = FunctionParams()
        for ((paramIndex, param) in methodInformation.parameters.withIndex()) {
            if (param.toString() == eventCanonicalName) {
                functionParams.eventParam = Param(param, paramIndex)
            } else if (isAListType(param) && param.toString().contains(eventCanonicalName)) {
                functionParams.eventParam = Param(param, paramIndex)
            } else {
                functionParams.inputParam = Param(param, paramIndex)
            }
        }
        return functionParams
    }

    protected data class FunctionParams(
            var inputParam: Param = Param(null, -1),
            var eventParam: Param = Param(null, -1)
    )

    protected fun isAListType(type: TypeMirror): Boolean {
        return type.toString().startsWith("java.util.List<")
    }

    protected fun findListType(list: TypeMirror): String {
        return list.toString().substringAfter("<").substringBefore(">")
    }

    protected data class Param(val type: TypeMirror?, val index: Int) {
        fun doesNotExist(): Boolean {
            return type == null
        }

        fun exists(): Boolean {
            return type != null
        }

        fun canonicalName(): String {
            if (type?.kind == TypeKind.VOID) return "java.lang.Void"
            return type?.toString() ?: "java.lang.Void"
        }

        fun simpleName(): String {
            if (type?.kind == TypeKind.VOID) return "Void"
            return type?.toString()?.substringAfterLast('.') ?: "Void"
        }
    }

}