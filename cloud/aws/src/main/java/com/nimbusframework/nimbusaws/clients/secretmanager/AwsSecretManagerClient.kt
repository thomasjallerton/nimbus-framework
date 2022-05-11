package com.nimbusframework.nimbusaws.clients.secretmanager

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

class AwsSecretManagerClient(
    private val client: SecretsManagerClient
): SecretManagerClient {

    override fun getSecret(secretName: String): String {
        val response = client.getSecretValue { it.secretId(secretName) }
        return response.secretString()
    }

}
