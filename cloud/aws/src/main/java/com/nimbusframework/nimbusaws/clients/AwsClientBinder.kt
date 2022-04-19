package com.nimbusframework.nimbusaws.clients

object AwsClientBinder {

    fun setInternalBuilder(internalAwsClientBuilder: InternalAwsClientBuilder) {
        AwsClientBuilder.internalClientBuilder = internalAwsClientBuilder
    }

}
