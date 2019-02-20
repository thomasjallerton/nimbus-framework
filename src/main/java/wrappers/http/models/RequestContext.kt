package wrappers.http.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestContext(
        val path: String? = null,
        val accountId: String? = null,
        val resourceId: String? = null,
        val stage: String? = null,
        val domainPrefix: String? = null,
        val requestId: String? = null,
        val identity: Identity? = null,
        val domainName: String? = null,
        val resourcePath: String? = null,
        val httpMethod: String? = null,
        val extendedRequestId: String? = null,
        val apiId: String? = null
)