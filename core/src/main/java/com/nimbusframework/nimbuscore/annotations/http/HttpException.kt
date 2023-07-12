package com.nimbusframework.nimbuscore.annotations.http

import java.lang.RuntimeException

class HttpException(
    val statusCode: Int,
    message: String
): RuntimeException(message)
