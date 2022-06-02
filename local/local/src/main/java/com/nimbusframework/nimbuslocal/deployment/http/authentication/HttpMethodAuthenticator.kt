package com.nimbusframework.nimbuslocal.deployment.http.authentication

import com.nimbusframework.nimbuslocal.deployment.http.HttpRequest

interface HttpMethodAuthenticator {

    fun allow(httpRequest: HttpRequest): AuthenticationResponse

}
