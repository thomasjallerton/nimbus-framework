package com.nimbusframework.nimbuscore.annotations.http

object CorsInformation {

    val allowedHeaders = setOf(
            "accept",
            "accept-language",
            "content-language",
            "origin",
            "user-agent",
            "content-type",
            "content-encoding",
            "accept-encoding"
    )

}
