package com.nimbusframework.nimbuslocal.deployment.http.authentication

data class AuthenticationResponse(
    val authenticated: Boolean,
    val context: Map<String, Any> = mapOf()
)
