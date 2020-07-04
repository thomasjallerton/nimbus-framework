package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload

class FileUploadAnnotation(private val fileUpload: FileUpload): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return fileUpload.fileStorageBucket.java
    }

}