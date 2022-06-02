package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.google.common.cache.CacheBuilder
import com.nimbusframework.nimbusawslocal.aws.LocalAwsResourceHolder
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.deployment.http.authentication.AuthenticationResponse
import com.nimbusframework.nimbuslocal.deployment.http.authentication.HttpMethodAuthenticator
import java.time.Duration.*

class CognitoHttpMethodAuthenticator(
    private val cognitoClass: Class<*>,
    private val headerName: String,
    ttl: Int,
    private val resourceHolder: LocalAwsResourceHolder
): HttpMethodAuthenticator {

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(ofSeconds(ttl.toLong()))
        .build<String, Boolean>()

    override fun allow(httpRequest: HttpRequest): AuthenticationResponse {
        val cognito = resourceHolder.cognitoUserPools[cognitoClass] ?: error("Cognito user pool not created - was it included in the list of classes / packages supplied to the local nimbus deployment")
        val authHeader = httpRequest.headers[headerName]?.firstOrNull() ?: return AuthenticationResponse(false)
        val cachedResult = cache.getIfPresent(authHeader)
        if (cachedResult != null) {
            return AuthenticationResponse(cachedResult)
        }
        val result = cognito.getUser(authHeader) != null
        cache.put(authHeader, result)
        return AuthenticationResponse(result)
    }

}
