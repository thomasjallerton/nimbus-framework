package com.nimbusframework.nimbuscore.wrappers.file.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FileRecord (
    val s3: S3Field = S3Field()
)