package com.nimbusframework.nimbuscore.annotations.function

import java.lang.RuntimeException

class HttpException(
    val statusCode: Int,
    message: String
): RuntimeException(message)
