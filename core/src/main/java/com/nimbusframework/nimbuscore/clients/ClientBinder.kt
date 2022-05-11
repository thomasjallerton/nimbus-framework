package com.nimbusframework.nimbuscore.clients

object ClientBinder {

    fun setInternalBuilder(internalClientBuilder: InternalClientBuilder) {
        ClientBuilder.internalClientBuilder = internalClientBuilder
    }

}
