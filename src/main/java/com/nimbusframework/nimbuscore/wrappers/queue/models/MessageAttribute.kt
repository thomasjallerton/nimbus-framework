package com.nimbusframework.nimbuscore.wrappers.queue.models


class MessageAttribute(
        val stringValue: String? = null,
        val stringListValues: List<String>? = null,
        val binaryListValues: List<String>? = null,
        val dataType: String? = null,
        val binaryValue: String? = null
)