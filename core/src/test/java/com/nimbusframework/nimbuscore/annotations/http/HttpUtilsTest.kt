package com.nimbusframework.nimbuscore.annotations.http

import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

class HttpUtilsTest: StringSpec({

    "Can decompress gzip encoded request" {
        val request = HttpEvent(
            body = "H4sIAAAAAAAAAPNw9fHxVwj3D/JxAQBbhuWHCwAAAA==",
            headers = mapOf(Pair("content-encoding", "gzip")),
            isBase64Encoded = true
        )

        HttpUtils.getUncompressedContent(request) shouldBe "HELLO WORLD"
    }

    "Can compress response for request that accepts encoding" {
        val request = HttpEvent(
            headers = mapOf(Pair("accept-encoding", "gzip")),
        )

        val result = HttpUtils.compressContent(request, "HELLO WORLD")
        result!!.encoding shouldBe "gzip"
        Base64.getEncoder().encodeToString(result.content) shouldBe "H4sIAAAAAAAAAPNw9fHxVwj3D/JxAQBbhuWHCwAAAA=="
    }

    "Will not compress response for request that accepts nothing" {
        HttpUtils.compressContent(HttpEvent(), "HELLO WORLD") shouldBe null
    }

    "Will not compress response for request that accepts identity" {
        val request = HttpEvent(
            headers = mapOf(Pair("accept-encoding", "identity")),
        )

        HttpUtils.compressContent(request, "HELLO WORLD") shouldBe null
    }

    "Will not compress response for request that accepts *" {
        val request = HttpEvent(
            headers = mapOf(Pair("accept-encoding", "*")),
        )

        HttpUtils.compressContent(request, "HELLO WORLD") shouldBe null
    }

    "Can compress response for request that accepts multiple encodings - supported encoding first" {
        val request = HttpEvent(
            headers = mapOf(Pair("accept-encoding", "gzip, deflate, br")),
        )

        val result = HttpUtils.compressContent(request, "HELLO WORLD")
        result!!.encoding shouldBe "gzip"
        Base64.getEncoder().encodeToString(result.content) shouldBe "H4sIAAAAAAAAAPNw9fHxVwj3D/JxAQBbhuWHCwAAAA=="
    }

    "Can compress response for request that accepts multiple encodings - supported encoding last" {
        val request = HttpEvent(
            headers = mapOf(Pair("accept-encoding", "deflate, br, gzip")),
        )

        val result = HttpUtils.compressContent(request, "HELLO WORLD")
        result!!.encoding shouldBe "gzip"
        Base64.getEncoder().encodeToString(result.content) shouldBe "H4sIAAAAAAAAAPNw9fHxVwj3D/JxAQBbhuWHCwAAAA=="
    }


})
