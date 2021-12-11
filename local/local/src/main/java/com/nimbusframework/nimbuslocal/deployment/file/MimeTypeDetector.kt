package com.nimbusframework.nimbuslocal.deployment.file

object MimeTypeDetector {

    fun guessMimeTypeByExtension(fileName: String): String? {
        return when (fileName.substringAfterLast('.')) {
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            else -> null
        }
    }
}
