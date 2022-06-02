package com.nimbusframework.nimbusawslocal.aws.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse
import com.google.common.cache.CacheBuilder
import com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry.CustomContext
import com.nimbusframework.nimbuslocal.deployment.function.FunctionIdentifier
import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest
import com.nimbusframework.nimbuslocal.deployment.http.authentication.AuthenticationResponse
import com.nimbusframework.nimbuslocal.deployment.http.authentication.HttpMethodAuthenticator
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import java.time.Duration.*
import java.util.*

class LambdaHttpMethodAuthenticator(
    private val functionIdentifier: FunctionIdentifier,
    private val headerName: String,
    ttl: Int,
    private val resourceHolder: LocalResourceHolder
): HttpMethodAuthenticator {

    private val ARN_PREFIX = "arn:aws:execute-api:"
    private val LOCAL_REGION = "local-here-1"
    private val ACCOUNT_ID = "000000000"
    private val API_ID = "111111111"
    private val LOCAL_STAGE = "local"

    private val cache = CacheBuilder.newBuilder()
        .expireAfterWrite(ofSeconds(ttl.toLong()))
        .build<String, IamPolicyResponse>()

    override fun allow(httpRequest: HttpRequest): AuthenticationResponse {
        val authHeader = httpRequest.headers[headerName]?.firstOrNull() ?: return AuthenticationResponse(false)
        val cachedResult = cache.getIfPresent(authHeader)
        val policy = if (cachedResult != null) {
            cachedResult
        } else {
            val authFunction = resourceHolder.functions[functionIdentifier]!!.serverlessMethod as AuthorizationFunction

            val arn = "$ARN_PREFIX${LOCAL_REGION}:$ACCOUNT_ID:$API_ID/$LOCAL_STAGE/${httpRequest.method.name}/${httpRequest.path}]"
            val event = APIGatewayCustomAuthorizerEvent.builder()
                .withMethodArn(arn)
                .withAuthorizationToken(authHeader)
                .withHttpMethod(httpRequest.method.name)
                .withHeaders(httpRequest.headers.mapValues { it.value.joinToString { "," } })
                .withPath(httpRequest.path)
                .build()

            val newPolicy = authFunction.invokeMethod(event, CustomContext(UUID.randomUUID().toString()))
            cache.put(authHeader, newPolicy)
            newPolicy
        }

        return AuthenticationResponse(validatePolicy(httpRequest, policy), policy.context)
    }


    private fun validatePolicy(httpRequest: HttpRequest, policyResponse: IamPolicyResponse): Boolean {
        val statements = policyResponse.policyDocument["Statement"]!! as Array<Map<String, Any>>
        val statement = statements.firstOrNull {
            val resource = it["Resource"]!! as Array<String>
            matches(httpRequest, resource[0])
        } ?: return false
        return statement["Effect"] == "Allow"
    }

    private fun matches(httpRequest: HttpRequest, resource: String): Boolean {
        if (resource.startsWith(ARN_PREFIX)) {
            val remainingArn = resource.substringAfter(ARN_PREFIX).split(":")

            // Validate region and account id
            if ((remainingArn[0] != "*" && remainingArn[0] != LOCAL_REGION) || (remainingArn[1] != "*" && remainingArn[1] != ACCOUNT_ID)) {
                return false
            }

            val apiIdAndPath = remainingArn[2].removePrefix("/").split("/")

            if (apiIdAndPath[0] != "*" && apiIdAndPath[0] != API_ID) {
                return false
            }

            if (apiIdAndPath.getOrNull(1) != null && apiIdAndPath.getOrNull(1) != "*" && apiIdAndPath.getOrNull(1) != LOCAL_STAGE) {
                return false
            }

            if (apiIdAndPath.getOrNull(2) != null && apiIdAndPath.getOrNull(2) != "*" && apiIdAndPath.getOrNull(2) != httpRequest.method.name) {
                return false
            }

            if (apiIdAndPath.last() == "*") {
                return true
            }

            // validate path
            val path = apiIdAndPath.drop(3).toMutableList()
            val requestPath = httpRequest.path.removePrefix("/").split("/").toMutableList()

            while (path.isNotEmpty() && requestPath.isNotEmpty()) {
                if (requestPath[0] != path[0]) {
                    return false
                }
                path.removeAt(0)
                requestPath.removeAt(0)
            }

            return path.isEmpty() && requestPath.isEmpty()
        }
        return false
    }

}
