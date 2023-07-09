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
        if (httpEvent.multiValueHeaders?.containsKey(CONTENT_ENCODING_HEADER) == true) {
            return httpEvent.multiValueHeaders[CONTENT_ENCODING_HEADER]!!.map { ContentEncoding.forContentEncodingHeader(it) }
        }
        return listOf()
    }

    /**
     * Returns the accepted encodings, in order of preference.
     */
    @JvmStatic
    fun getAcceptEncodings(headers: Map<String, String>?, multiValueHeaders: Map<String, List<String>>?): ContentEncoding? {
        if (headers?.containsKey(ACCEPT_ENCODING_HEADER) == true) {
            return ContentEncoding.forAcceptEncodingHeader(headers[ACCEPT_ENCODING_HEADER]!!)
        }
        if (multiValueHeaders?.containsKey(ACCEPT_ENCODING_HEADER) == true) {
            return multiValueHeaders[ACCEPT_ENCODING_HEADER]!!.firstNotNullOfOrNull { ContentEncoding.forAcceptEncodingHeader(it) }
        }
        return null
    }

    @JvmStatic
    fun getUncompressedContent(httpEvent: HttpEvent): String {
        val encodings = getContentEncoding(httpEvent)
        var current = getBase64Decoded(httpEvent.body!!)
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
    private fun getBase64Decoded(input: String): ByteArray {
        return try {
            val trimmedInput = input.removePrefix("\"").removeSuffix("\"")
            Base64.getDecoder().decode(trimmedInput)
        } catch (e: Exception) {
            input.toByteArray()
        }
    }

    @JvmStatic
    fun compressContent(httpEvent: HttpEvent, toCompress: String): CompressedContent? {
        return compressContent(httpEvent.headers, httpEvent.multiValueHeaders, toCompress)
    }

    @JvmStatic
    fun compressContent(headers: Map<String, String>?, multiValueHeaders: Map<String, List<String>>?, toCompress: String): CompressedContent? {
        headers?.forEach { println("${it.key}, ${it.value}") }
        multiValueHeaders?.forEach { println("${it.key}, [${it.value.joinToString(",")}]") }
        val chosenEncoding = getAcceptEncodings(headers, multiValueHeaders)
        if (chosenEncoding == null) {
            return null
        }
        val content = when (chosenEncoding) {
            ContentEncoding.GZIP -> {
                val bos = ByteArrayOutputStream()
                val compressionStream = GZIPOutputStream(bos)
                compressionStream.write(toCompress.encodeToByteArray())
                compressionStream.flush()
                compressionStream.close()
                bos.toByteArray()
            }
            ContentEncoding.IDENTITY, ContentEncoding.NO_PREFERENCE -> return null
        }
        println(chosenEncoding)
        return CompressedContent(content, chosenEncoding.header)
    }

    const val CONTENT_ENCODING_HEADER = "Content-Encoding"
    private const val ACCEPT_ENCODING_HEADER = "Accept-Encoding"

    data class CompressedContent(val content: ByteArray, val encoding: String)
}
