package com.nimbusframework.nimbuscore.annotations.http

import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HttpUtilsTest: StringSpec({

    "Can decompress gzip encoded request" {
        val request = HttpEvent(
            body = "H4sIAAAAAAAAAzWLuw3DMAwFd2GtCbxBGi8QuKAs6hPQFEAyhRFk99AB3B3u3vtAIyFFn/oosABagQR1qPmKBxksT8jkPdXQr9klHQFonekM2NH/JQ/mM9KWwN4q9xOZSX1KLJqOWsdFbgOlXYrRfAYQZ6XiHbbvDxe61G+QAAAA",
            headers = mapOf(Pair("Content-Encoding", "gzip"))
        )

        HttpUtils.getUncompressedContent(request) shouldBe "{\"generatorId\":\"asd\",\"firstNames\":[\"beth,f\",\"john,m\",\"ashley\",\"cat,f\",\"billy,m\"],\"surnames\":[\"allerton\",\"griffin\",\"tsiang\",\"glasto\",\"elbredth\"]}"
    }

    "Can decompress gzip encoded request surrounded by quotes" {
        val request = HttpEvent(
            body = "\"H4sIAAAAAAAAAzWLuw3DMAwFd2GtCbxBGi8QuKAs6hPQFEAyhRFk99AB3B3u3vtAIyFFn/oosABagQR1qPmKBxksT8jkPdXQr9klHQFonekM2NH/JQ/mM9KWwN4q9xOZSX1KLJqOWsdFbgOlXYrRfAYQZ6XiHbbvDxe61G+QAAAA\"",
            headers = mapOf(Pair("Content-Encoding", "gzip"))
        )

        HttpUtils.getUncompressedContent(request) shouldBe "{\"generatorId\":\"asd\",\"firstNames\":[\"beth,f\",\"john,m\",\"ashley\",\"cat,f\",\"billy,m\"],\"surnames\":[\"allerton\",\"griffin\",\"tsiang\",\"glasto\",\"elbredth\"]}"
    }


})
