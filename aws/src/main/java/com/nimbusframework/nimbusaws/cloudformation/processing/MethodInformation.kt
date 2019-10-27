package com.nimbusframework.nimbusaws.cloudformation.processing

import javax.lang.model.type.TypeMirror

data class MethodInformation(
        val className: String = "",
        val methodName: String = "",
        val packageName: String = "",
        val parameters: List<TypeMirror> = listOf(),
        val returnType: TypeMirror
)