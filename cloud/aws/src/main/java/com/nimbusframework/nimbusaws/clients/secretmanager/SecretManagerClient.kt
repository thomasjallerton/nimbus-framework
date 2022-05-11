package com.nimbusframework.nimbusaws.clients.secretmanager

interface SecretManagerClient {

    fun getSecret(secretName: String): String

}
