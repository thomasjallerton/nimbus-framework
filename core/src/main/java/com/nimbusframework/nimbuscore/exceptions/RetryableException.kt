package com.nimbusframework.nimbuscore.exceptions

class RetryableException(message: String): Exception("The transaction can be retried, exception was $message")