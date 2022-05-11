package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbusaws.clients.InternalAwsClientBuilder
import com.nimbusframework.nimbusaws.clients.cognito.CognitoClient
import com.nimbusframework.nimbusaws.clients.secretmanager.SecretManagerClient
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognitoClient
import com.nimbusframework.nimbusawslocal.aws.secretsmanager.LocalSecretManagerClient

object InternalAwsLocalClientBuilder: InternalAwsClientBuilder {

    override fun getCognitoClient(userPool: Class<*>, stage: String): CognitoClient {
        return LocalCognitoClient(AwsSpecificLocalDeployment.currentInstance().getUserPool(userPool))
    }

    override fun getSecretClient(): SecretManagerClient {
        return LocalSecretManagerClient(AwsSpecificLocalDeployment.currentInstance().getLocalSecrets())
    }

}
