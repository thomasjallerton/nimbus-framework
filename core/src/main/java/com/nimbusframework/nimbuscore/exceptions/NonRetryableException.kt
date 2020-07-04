package com.nimbusframework.nimbuscore.exceptions

import java.lang.RuntimeException

open class NonRetryableException(message: String): RuntimeException("The transaction cannot be retried due to $message")