package com.nimbusframework.nimbuscore.testing.function.permissions

class FileStoragePermission(
        private val bucketName: String
): Permission {
    override fun hasPermission(value: String): Boolean {
        return value == bucketName
    }
}