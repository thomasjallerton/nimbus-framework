package com.nimbusframework.nimbusaws.clients.cognito

data class CognitoUser(
    val userName: String,
    val attributes: Map<String, String>
)
