package com.nimbusframework.nimbusaws.clients.cognito

data class CognitoUser(
    val username: String,
    val attributes: Map<String, String>
)
