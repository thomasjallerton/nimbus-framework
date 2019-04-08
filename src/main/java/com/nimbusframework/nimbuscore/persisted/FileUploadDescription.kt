package com.nimbusframework.nimbuscore.persisted


data class FileUploadDescription(
        val localFile: String = "",
        val targetFile: String = "",
        val substituteVariables: Boolean = false
)