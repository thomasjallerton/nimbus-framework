package com.nimbusframework.nimbusawslocal.aws.secretsmanager

class LocalSecrets {

    private val secrets: MutableMap<String, SecretData> = mutableMapOf()

    fun getSecret(secretName: String): SecretData? {
        return secrets[secretName]
    }

    fun addSecret(secretName: String, secretArn: String, secretValue: String) {
        secrets[secretName] = SecretData(secretArn, secretValue)
    }

    data class SecretData(val arn: String, val value: String)

}
