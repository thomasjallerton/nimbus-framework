package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognito
import com.nimbusframework.nimbusawslocal.aws.secretsmanager.LocalSecrets

class LocalAwsResourceHolder {

    val cognitoUserPools = mutableMapOf<Class<*>, LocalCognito>()
    val localSecrets = LocalSecrets()

}
