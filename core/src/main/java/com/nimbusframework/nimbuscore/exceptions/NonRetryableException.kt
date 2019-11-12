package com.nimbusframework.nimbuscore.exceptions

open class NonRetryableException(message: String): Exception("The transaction cannot be retried due to $message")