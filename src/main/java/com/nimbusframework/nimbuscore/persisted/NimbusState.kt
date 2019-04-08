package com.nimbusframework.nimbuscore.persisted

data class NimbusState(
        val projectName: String,
        val compilationTimeStamp: String,
        val afterDeployments: MutableMap<String, MutableList<String>> = mutableMapOf(),
        //Stage -> Bucket -> LocalFile -> RemoteFile
        val fileUploads: MutableMap<String, MutableMap<String, MutableList<FileUploadDescription>>> = mutableMapOf(),
        val exports: MutableMap<String, MutableList<ExportInformation>> = mutableMapOf()
)