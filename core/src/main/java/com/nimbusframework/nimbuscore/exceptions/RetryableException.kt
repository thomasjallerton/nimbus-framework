package com.nimbusframework.nimbuscore.exceptions

import java.lang.RuntimeException

class RetryableException(message: String): RuntimeException("The transaction can be retried, exception was $message")