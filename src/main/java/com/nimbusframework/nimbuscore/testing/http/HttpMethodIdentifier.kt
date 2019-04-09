package com.nimbusframework.nimbuscore.testing.http

import com.nimbusframework.nimbuscore.annotation.annotations.function.HttpMethod

data class HttpMethodIdentifier(val path: String, val method: HttpMethod)
