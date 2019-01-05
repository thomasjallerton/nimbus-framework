package wrappers.http.models

data class Identity(
        val cognitoIdentityPoolId: String? = null,
        val cognitoIdentityId: String? = null,
        val apiKey: String? = null,
        val cognitoAuthenticationType: String? = null,
        val userArn: String? = null,
        val apiKeyId: String? = null,
        val userAgent: String? = null,
        val accountId: String? = null,
        val caller: String? = null,
        val sourceIp: String? = null,
        val accessKey: String? = null,
        val cognitoAuthenticationProvider: String? = null,
        val user: String? = null
)