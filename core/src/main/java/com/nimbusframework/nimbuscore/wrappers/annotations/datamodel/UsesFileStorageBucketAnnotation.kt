package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket

class UsesFileStorageBucketAnnotation(private val fileStorageServerlessFunction: UsesFileStorageBucket): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return fileStorageServerlessFunction.fileStorageBucket.java
    }

}