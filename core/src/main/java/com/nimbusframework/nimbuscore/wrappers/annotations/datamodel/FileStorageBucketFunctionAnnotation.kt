package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction

class FileStorageBucketFunctionAnnotation(private val fileStorageServerlessFunction: FileStorageServerlessFunction): DataModelAnnotation() {

    override val stages = fileStorageServerlessFunction.stages

    override fun internalDataModel(): Class<out Any> {
        return fileStorageServerlessFunction.fileStorageBucket.java
    }

}