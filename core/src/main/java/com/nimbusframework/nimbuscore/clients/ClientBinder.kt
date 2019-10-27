package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.database.InternalClientBuilder

object ClientBinder {

    fun setInternalBuilder(internalClientBuilder: InternalClientBuilder) {
        val fields = ClientBuilder.javaClass.declaredFields
        fields.forEach {field ->
            if (field.type == InternalClientBuilder::class.java) {
                field.isAccessible = true
                field.set(ClientBuilder, internalClientBuilder)
            }
        }
    }
}