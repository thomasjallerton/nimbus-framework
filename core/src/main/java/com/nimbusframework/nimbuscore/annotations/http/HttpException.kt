package com.nimbusframework.nimbuscore.annotations.http

import java.lang.RuntimeException

class HttpException(
    val statusCode: Int,
    message: String,
    /** If the exception should be printed to the console when caught. */
    val logException: Boolean = statusCode >= 500
): RuntimeException(message)
