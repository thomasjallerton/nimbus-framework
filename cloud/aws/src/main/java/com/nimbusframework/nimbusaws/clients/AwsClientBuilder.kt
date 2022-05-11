package com.nimbusframework.nimbusaws.clients

import com.nimbusframework.nimbusaws.clients.cognito.CognitoClient
import com.nimbusframework.nimbusaws.clients.secretmanager.SecretManagerClient
import com.nimbusframework.nimbuscore.clients.ClientBuilder

object AwsClientBuilder {

    internal lateinit var internalClientBuilder: InternalAwsClientBuilder

    @JvmStatic
    fun getCognitoClient(cognitoClient: Class<*>): CognitoClient {
        return internalClientBuilder.getCognitoClient(cognitoClient, ClientBuilder.getStage())
    }

    @JvmStatic
    fun getSecretManagerClient(): SecretManagerClient {
        return internalClientBuilder.getSecretClient()
    }

}
