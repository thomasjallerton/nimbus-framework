package com.nimbusframework.nimbuscore.clients

import com.nimbusframework.nimbuscore.clients.database.InternalClientBuilder

object ClientBinder {

    fun setInternalBuilder(internalClientBuilder: InternalClientBuilder) {
        ClientBuilder.internalClientBuilder = internalClientBuilder
    }

}
