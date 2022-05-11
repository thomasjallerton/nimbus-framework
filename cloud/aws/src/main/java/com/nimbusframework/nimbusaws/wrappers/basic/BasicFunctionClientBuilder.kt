package com.nimbusframework.nimbusaws.wrappers.basic

import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.FileBuilder
import com.nimbusframework.nimbuscore.clients.ClientBuilder
import com.nimbusframework.nimbuscore.clients.function.BasicServerlessFunctionClient
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent
import java.io.PrintWriter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeKind

class BasicFunctionClientBuilder(
    private val functionClassName: String,
    private val packageLocation: String,
    private val methods: Set<FileBuilderMethodInformation>,
    processingEnvironment: ProcessingEnvironment
): FileBuilder() {

    val className = functionClassName + "Serverless"

    private val functionCanonicalName: String = if (packageLocation.isEmpty()) functionClassName else "$packageLocation.$functionClassName"

    init {
        val builderFile = processingEnvironment.filer.createSourceFile(className)
        out = PrintWriter(builderFile.openWriter())
    }

    private fun writeImports() {
        import(functionCanonicalName)
        import(BasicEvent::class.qualifiedName!!)
        import(ClientBuilder::class.qualifiedName!!)
        import(BasicServerlessFunctionClient::class.qualifiedName!!)
    }

    fun writeInterfaceClass() {
        write("package $packageLocation;")
        write()
        writeImports()

        write("public class $className extends $functionClassName {")

        for (methodInformation in methods) {
            overrideFunction(methodInformation)
        }

        write("}")

        out?.close()
    }

    private fun overrideFunction(fileBuilderMethodInformation: FileBuilderMethodInformation) {
        val voidMethodReturn = fileBuilderMethodInformation.returnType.kind == TypeKind.NULL || fileBuilderMethodInformation.returnType.kind == TypeKind.VOID

        val (inputParam, eventParam) = findParamIndexes(fileBuilderMethodInformation, BasicEvent::class.qualifiedName!!)

        val paramDeclaration = when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> "()"
            inputParam.doesNotExist() -> "(BasicEvent event)"
            eventParam.doesNotExist() -> "(${inputParam.type!!} inputType)"
            inputParam.index == 0 -> "(${inputParam.type!!} inputType, BasicEvent event)"
            else -> "(BasicEvent event, ${inputParam.type!!} inputType)"
        }

        write("@Override")
        write("public ${fileBuilderMethodInformation.returnType} ${fileBuilderMethodInformation.methodName}$paramDeclaration {")
        write("${BasicServerlessFunctionClient::class.simpleName} functionClient = ${ClientBuilder::class.simpleName}.getBasicServerlessFunctionClient($functionClassName.class, \"${fileBuilderMethodInformation.methodName}\");")

        val invocationType = when {
            voidMethodReturn && inputParam.doesNotExist() -> "functionClient.invoke();"
            voidMethodReturn && inputParam.exists() -> "functionClient.invoke(inputType);"
            inputParam.doesNotExist() -> "return functionClient.invoke(${fileBuilderMethodInformation.returnType}.class);"
            else -> "return functionClient.invoke(inputType, ${fileBuilderMethodInformation.returnType}.class);"
        }

        write(invocationType)

        write("}")
    }

}
