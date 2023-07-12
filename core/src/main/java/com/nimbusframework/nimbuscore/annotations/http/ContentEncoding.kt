package com.nimbusframework.nimbuscore.annotations.http

enum class ContentEncoding(val header: String) {
    GZIP("gzip"),
    IDENTITY("identity"),
    NO_PREFERENCE("*");

    companion object {
        fun forContentEncodingHeader(header: String): ContentEncoding {
            return ContentEncoding.values().firstOrNull { it.header == header } ?: error("Unsupported content encoding")
        }

        fun forAcceptEncodingHeader(header: String): ContentEncoding? {
            val splitHeader = header.split(",").map { it.trim() }
            for (headerPart in splitHeader) {
                val qualityHeaders = headerPart.split(";").map { it.trim() }
                return values().firstOrNull { it.header == qualityHeaders[0] } ?: continue
            }
            return null
        }

    }
}
