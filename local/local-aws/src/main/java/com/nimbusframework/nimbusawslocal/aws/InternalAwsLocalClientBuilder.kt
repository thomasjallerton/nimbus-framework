package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbusaws.clients.InternalAwsClientBuilder
import com.nimbusframework.nimbusaws.clients.cognito.CognitoClient
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognitoClient

object InternalAwsLocalClientBuilder: InternalAwsClientBuilder {

    override fun getCognitoClient(userPool: Class<*>, stage: String): CognitoClient {
        return LocalCognitoClient(AwsSpecificLocalDeployment.currentInstance().getUserPool(userPool))
    }

}
