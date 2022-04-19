package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognito

class LocalAwsResourceHolder {

    val cognitoUserPools = mutableMapOf<Class<*>, LocalCognito>()

}
