package com.nimbusframework.nimbusaws.cloudformation.model.processing

import javax.lang.model.type.TypeMirror

data class FileBuilderMethodInformation(
        val className: String = "",
        val customFactoryQualifiedName: String?,
        val methodName: String = "",
        val packageName: String = "",
        val parameters: List<TypeMirror> = listOf(),
        val returnType: TypeMirror
) {

        fun getQualifiedClassName(): String {
                if (packageName.isBlank()) {
                        return className
                }
                return "$packageName.$className"
        }

}
