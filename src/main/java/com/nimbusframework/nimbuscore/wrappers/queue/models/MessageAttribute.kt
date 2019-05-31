package com.nimbusframework.nimbuscore.wrappers.queue.models

import java.nio.ByteBuffer


class MessageAttribute(
        val stringValue: String? = null,
        val stringListValues: List<String>? = null,
        val binaryListValues: List<ByteBuffer>? = null,
        val dataType: String? = null,
        val binaryValue: ByteBuffer? = null
)