package com.nimbusframework.nimbuscore.annotations.http

import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object HttpUtils {

    @JvmStatic
    fun getContentEncoding(httpEvent: HttpEvent): List<ContentEncoding> {
        if (httpEvent.headers?.containsKey(CONTENT_ENCODING_HEADER) == true) {
            return listOf(ContentEncoding.forContentEncodingHeader(httpEvent.headers[CONTENT_ENCODING_HEADER]!!))
        }
        return listOf()
    }

    /**
     * Returns the accepted encodings, in order of preference.
     */
    @JvmStatic
    fun getAcceptEncodings(headers: Map<String, String>?): ContentEncoding? {
        if (headers?.containsKey(ACCEPT_ENCODING_HEADER) == true) {
            return ContentEncoding.forAcceptEncodingHeader(headers[ACCEPT_ENCODING_HEADER]!!)
        }
        return null
    }

    @JvmStatic
    fun getUncompressedContent(httpEvent: HttpEvent): String {
        val encodings = getContentEncoding(httpEvent)
        var current = getBase64Decoded(httpEvent.body!!, httpEvent.isBase64Encoded!!)
        // Reversed as the header is in the order the encodings were applied
        for (compression in encodings.reversed()) {
            when (compression) {
                ContentEncoding.GZIP -> {
                    val uncompressedStream = GZIPInputStream(ByteArrayInputStream(current))
                    current = uncompressedStream.bufferedReader().use(BufferedReader::readText).toByteArray()
                }
                ContentEncoding.IDENTITY, ContentEncoding.NO_PREFERENCE -> {
                    // Do nothing
                }
            }
        }
        return String(current)
    }

    // Tries to decode the input as a base 64 string.
    private fun getBase64Decoded(input: String, isBase64Encoded: Boolean): ByteArray {
        return if (isBase64Encoded) {
            Base64.getDecoder().decode(input)
        } else {
            input.toByteArray()
        }
    }

    @JvmStatic
    fun compressContent(httpEvent: HttpEvent, toCompress: String): CompressedContent? {
        return compressContent(httpEvent.headers, toCompress.toByteArray())
    }

    @JvmStatic
    fun compressContent(httpEvent: HttpEvent, toCompress: ByteArray): CompressedContent? {
        return compressContent(httpEvent.headers, toCompress)
    }

    @JvmStatic
    fun compressContent(headers: Map<String, String>?, toCompress: ByteArray): CompressedContent? {
        val chosenEncoding = getAcceptEncodings(headers) ?: return null
        val content = when (chosenEncoding) {
            ContentEncoding.GZIP -> {
                val bos = ByteArrayOutputStream()
                val compressionStream = GZIPOutputStream(bos)
                compressionStream.write(toCompress)
                compressionStream.flush()
                compressionStream.close()
                bos.toByteArray()
            }
            ContentEncoding.IDENTITY, ContentEncoding.NO_PREFERENCE -> return null
        }
        return CompressedContent(content, chosenEncoding.header)
    }

    const val CONTENT_ENCODING_HEADER = "content-encoding"
    private const val ACCEPT_ENCODING_HEADER = "accept-encoding"

    data class CompressedContent(val content: ByteArray, val encoding: String)
}
