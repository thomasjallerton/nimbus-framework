package com.nimbusframework.nimbuscore.eventabstractions

import java.nio.ByteBuffer


class StoreMessageAttribute(
        val stringValue: String? = null,
        val stringListValues: List<String>? = null,
        val binaryListValues: List<ByteBuffer>? = null,
        val dataType: String? = null,
        val binaryValue: ByteBuffer? = null
)