package com.nimbusframework.nimbusaws.cloudformation.processing

import javax.lang.model.type.TypeMirror

data class FileBuilderMethodInformation(
        val className: String = "",
        val customFactoryQualifiedName: String?,
        val methodName: String = "",
        val packageName: String = "",
        val parameters: List<TypeMirror> = listOf(),
        val returnType: TypeMirror
)
